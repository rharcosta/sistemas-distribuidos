package conexoes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

public class TCPServerAtivosMain extends Thread implements ActionListener {

    private List<TCPServerConnection> clientes;
    private ServerSocket server;
    int larguraTela, alturaTela, area;
    int tamUnidade, delay;
    int macaX, macaY;
    int corpo1, corpo2;
    char direcao1, direcao2;
    int x1[], x2[];
    int y1[], y2[];
    boolean ativo;
    Timer timer;
    
    @Override
    public void actionPerformed(ActionEvent e) {
        andar();
    }

    public TCPServerAtivosMain(int porta) throws IOException {
        this.server = new ServerSocket(porta);
        System.out.println(this.getClass().getSimpleName() + " rodando na porta: " + server.getLocalPort());
        this.clientes = new ArrayList<>();
        larguraTela = 700;
        alturaTela = 480;
        tamUnidade = 20;
        area = (larguraTela * alturaTela) / (tamUnidade * tamUnidade);
        delay = 175;
        corpo1 = corpo2 = 3;
        direcao1 = 'D';
        direcao2 = 'E';
        x1 = y1 = x2 = y2 = new int[area];
        x2[0] = larguraTela;
        y2[0] = alturaTela;
        ativo = false;
        comida();
        timer = new Timer(delay, this);
        timer.start();
    }

    public void andar() {
        for (int i = corpo1; i > 0; i--) {
            x1[i] = x1[i - 1];
            y1[i] = y1[i - 1];
        }
        //ver que direção a cobrinha está indo para ela ir sozinha
        switch (direcao1) {
            case 'C':
                y1[0] = y1[0] - tamUnidade;
                break;
            case 'B':
                y1[0] = y1[0] + tamUnidade;
                break;
            case 'E':
                x1[0] = x1[0] - tamUnidade;
                break;
            case 'D':
                x1[0] = x1[0] + tamUnidade;
                break;
        }

        for (int i = corpo2; i > 0; i--) {
            x2[i] = x2[i - 1];
            y2[i] = y2[i - 1];
        }
        //ver que direção a cobrinha está indo para ela ir sozinha
        switch (direcao2) {
            case 'C':
                y2[0] = y2[0] - tamUnidade;
                break;
            case 'B':
                y2[0] = y2[0] + tamUnidade;
                break;
            case 'E':
                x2[0] = x2[0] - tamUnidade;
                break;
            case 'D':
                x2[0] = x2[0] + tamUnidade;
                break;
        }
        comer();
        checarColisao();
    }

    public void comida() {
        Random random = new Random();
        macaX = random.nextInt((int) (larguraTela / tamUnidade)) * tamUnidade;
        macaY = random.nextInt((int) (alturaTela / tamUnidade)) * tamUnidade;
    }

    public void comer() {
        if ((x1[0] == macaX) && (y1[0] == macaY)) {
            corpo1++;
            comida();
        }
        if ((x2[0] == macaX) && (y2[0] == macaY)) {
            corpo2++;
            comida();
        }
    }

    public void checarColisao() {
        //cobrinha 1
        //colisão com o próprio corpo 
        for (int i = corpo1; i > 0; i--) {
            if ((x1[0] == x1[i]) && (y1[0] == y1[i])) {
                ativo = false;
            }
        }
        //colisão com a outra cobrinha
        for (int i = corpo2; i > 0; i--) {
            if ((x1[0] == x2[i]) && (y1[0] == y2[i])) {
                ativo = false;
            }
        }
        //cabeça tocar na esquerda, direita, superior, inferior
        if (x1[0] < 0 || x1[0] > larguraTela || y1[0] < 0 || y1[0] > alturaTela) {
            ativo = false;
        }

        //cobrinha 2
        //colisão com o próprio corpo 
        for (int i = corpo2; i > 0; i--) {
            if ((x2[0] == x2[i]) && (y2[0] == y2[i])) {
                ativo = false;
            }
        }
        //colisão com a outra cobrinha
        for (int i = corpo1; i > 0; i--) {
            if ((x2[0] == x1[i]) && (y2[0] == y1[i])) {
                ativo = false;
            }
        }
        //cabeça tocar na esquerda, direita, superior, inferior
        if (x2[0] < 0 || x2[0] > larguraTela || y2[0] < 0 || y2[0] > alturaTela) {
            ativo = false;
        }

        if (!ativo) {
            timer.stop();
        }
    }

    @Override
    public void run() {
        Socket socket;
        while (true) {
            try {
                socket = this.server.accept();
                TCPServerConnection cliente = new TCPServerConnection(socket);
                novoCliente(cliente);
                (new TCPServerAtivosHandler(cliente, this)).start();
            } catch (IOException ex) {
                System.out.println("Erro 4: " + ex.getMessage());
            }
        }
    }

    public synchronized void novoCliente(TCPServerConnection cliente) throws IOException {
        cliente.id = clientes.size() + 1;
        clientes.add(cliente);
    }

    public synchronized void removerCliente(TCPServerConnection cliente) {
        clientes.remove(cliente);
        try {
            cliente.getInput().close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        cliente.getOutput().close();
        try {
            cliente.getSocket().close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List getClientes() {
        return clientes;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.server.close();
    }
}