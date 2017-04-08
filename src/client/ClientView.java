package client;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.DefaultCaret;

public class ClientView extends javax.swing.JFrame implements Observer {
	private static final long serialVersionUID = -5346255320116138428L;
	private JPanel contentPane;
	private JLabel lblAddress;
	private JLabel lblPort;
	private JLabel lblServer;
	private JTextPane textPane;
	private JButton btnDisconnectFromServer;
	private JButton btnAddServer;
	Client client;
	private JButton btnRunJob;

    public ClientView(Client client) {
    	setResizable(false);
        this.client = client;
        client.addObserver(this);
        initComponents();
        this.setSize(new Dimension(700,500));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {

    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(5, 5, 217, 452);
		contentPane.add(panel);
		
		btnDisconnectFromServer = new JButton("Disconnect from server");
		btnDisconnectFromServer.setBounds(0, 166, 215, 23);
		btnDisconnectFromServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.disconnect();
			}
		});
		panel.setLayout(null);
		panel.add(btnDisconnectFromServer);
		
		btnAddServer = new JButton("Connect to server");
		btnAddServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				String server = JOptionPane.showInputDialog(panel,
                        "Enter the server name", null);
				int port = Integer.parseInt(JOptionPane.showInputDialog(panel, "Enter the server port", null));
				InetSocketAddress serverAddress = new InetSocketAddress(server, port);
				client.connect(serverAddress);
			}
		});
		btnAddServer.setBounds(0, 132, 215, 23);
		panel.add(btnAddServer);
		
		lblPort = new JLabel("Port: ");
		lblPort.setBounds(10, 11, 195, 32);
		panel.add(lblPort);
		lblPort.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		lblAddress = new JLabel("Address:");
		lblAddress.setBounds(10, 46, 195, 32);
		panel.add(lblAddress);
		lblAddress.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		lblServer = new JLabel("Server:");
		lblServer.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblServer.setBounds(10, 78, 195, 32);
		panel.add(lblServer);
		
		btnRunJob = new JButton("Run job");
		btnRunJob.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				client.runJob();
			}
		});
		btnRunJob.setBounds(0, 200, 215, 23);
		panel.add(btnRunJob);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(221, 5, 458, 452);
		contentPane.add(panel_1);
		
		JLabel lblLogger = new JLabel("Logger:");
		lblLogger.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(lblLogger, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE)
					.addGap(38))
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(10)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(lblLogger)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		textPane = new JTextPane();
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		scrollPane.setViewportView(textPane);
		panel_1.setLayout(gl_panel_1);
    }


    public void update(Observable ob, Object o) {
        lblAddress.setText("Adress: " + client.getAddress());
        lblPort.setText("Port: " + client.getAddress().getPort());
        
        if (client.isConnected())
        	lblServer.setText("Server: " + client.getServerAddress().getAddress() + ":" + client.getServerAddress().getPort());
        else
        	lblServer.setText("Server: none");

        setTitle(client.getAddress().toString());
		textPane.setText(client.getLoggerText());
		
		btnDisconnectFromServer.setEnabled(client.isActive());
		btnAddServer.setEnabled(!client.isActive());
    }
}
