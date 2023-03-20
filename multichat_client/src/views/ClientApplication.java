package views;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;

import dto.request.RequestDto;
import lombok.Getter;
import lombok.Setter;

import java.awt.CardLayout;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Getter
public class ClientApplication extends JFrame {

	private static final long serialVersionUID = 1L;
	private static ClientApplication instance;
	
	
	private Gson gson;
	private Socket socket;
	
	private JPanel mainPanel;
	private CardLayout mainCard;
	
	private JTextField usernameField;
	
	private JTextField sendMessageField;
	
	@Setter
	private List<Map<String,String>> roomInfoList;
	private DefaultListModel<String> roomNameListModel;
	private DefaultListModel<String> usernameListModel;
	private JList roomList;
	private JList joinUserList;
	private JTextArea chattingContent;
	
	public static ClientApplication getInstance() {
		if(instance == null) {
			instance = new ClientApplication();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = ClientApplication.getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientApplication() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
				sendRequest(requestDto);
			}
		});
		
		/*============<< init >>================**/
		gson = new Gson();
		try {
			socket = new Socket("192.168.2.101", 9090);
			ClientReceive clientReceive = new ClientReceive(socket);
			clientReceive.start();
			
		} catch (UnknownHostException e1) {			
			e1.printStackTrace();			
		} catch (ConnectException e1) {
			JOptionPane.showMessageDialog(this, "서버에 접속할 수 없습니다.", "접속오류", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/*============<< frame set >>================**/
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 150, 480, 800);
		
		/*============<< panels >>================**/
		
		mainPanel = new JPanel();
		JPanel loginPanel = new JPanel();
		JPanel roomListPanel = new JPanel();
		JPanel roomPanel = new JPanel();
		
		/*============<< layout >>================**/
		
		mainCard = new CardLayout();
		
		mainPanel.setLayout(mainCard);
		loginPanel.setLayout(null);
		roomPanel.setLayout(null);
		roomListPanel.setLayout(null);

		/*============<< panel set >>================**/
		
		setContentPane(mainPanel);		
		mainPanel.add(loginPanel, "loginPanel");
		mainPanel.add(roomListPanel, "roomListPanel");
		mainPanel.add(roomPanel, "roomPanel");
		
		/*============<< login Panel >>================**/
		
		JButton enterButton = new JButton("접속하기");
		
		usernameField = new JTextField();
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestDto<String> usernameCheckReqDto = 
							new RequestDto<String>("usernameCheck", usernameField.getText());
					sendRequest(usernameCheckReqDto);
				}				
			}
		});
		
		usernameField.setBounds(47, 385, 366, 43);
		loginPanel.add(usernameField);
		usernameField.setColumns(10);
		
		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> usernameCheckReqDto = 
						new RequestDto<String>("usernameCheck", usernameField.getText());
				sendRequest(usernameCheckReqDto);
			}
		});
		enterButton.setBounds(47, 438, 366, 43);
		loginPanel.add(enterButton);
		
		/*============<< roomList Panel >>===============*/

		JScrollPane roomListScroll = new JScrollPane();
		roomListScroll.setBounds(92, 0, 364, 753);
		roomListPanel.add(roomListScroll);
		
		roomNameListModel = new DefaultListModel<String>();
		roomList = new JList(roomNameListModel);
		roomList.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int selectedIndex = roomList.getSelectedIndex();
					RequestDto<Map<String, String>> requestDto = 
							new RequestDto<Map<String, String>> ("enterRoom", roomInfoList.get(selectedIndex));
					sendRequest(requestDto);
				}
				
			}
		});
		roomListScroll.setViewportView(roomList);
		
		JButton createRoomButton = new JButton("방생성");
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = null;
				while(true) {
					roomName = JOptionPane.showInputDialog(null, "생성할 방의 제목을 입력하세요.","방생성",JOptionPane.PLAIN_MESSAGE);
					if(roomName == null) {
						return;
					}
					if(!roomName.isBlank()) {
						break;
					}
					JOptionPane.showConfirmDialog(null, "공백은 사용할 수 없습니다.", "방생성 오류", JOptionPane.ERROR_MESSAGE);		
				}
				RequestDto<String> requestDto = new RequestDto<String>("createRoom", roomName);
				sendRequest(requestDto);
			}
		});
		createRoomButton.setBounds(12, 59, 68, 65);
		roomListPanel.add(createRoomButton);
		
		/*============<< room Panel >>===============*/
		
		JScrollPane joinUserScroll = new JScrollPane();
		joinUserScroll.setBounds(0, 0, 315, 118);
		roomPanel.add(joinUserScroll);
		
		usernameListModel = new DefaultListModel<String>();
		joinUserList = new JList(usernameListModel);
		joinUserScroll.setViewportView(joinUserList);
		
		JButton roomExitBurron = new JButton("나가기");
		roomExitBurron.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(JOptionPane.showConfirmDialog(null, "정말로 방을 나가시겠습니까?", "방 나가기", JOptionPane.YES_NO_OPTION) == 0) {
				RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
				sendRequest(requestDto);
				}
			}
		});
		roomExitBurron.setBounds(317, 0, 139, 118);
		roomPanel.add(roomExitBurron);
		
		JScrollPane chattingContentScroll = new JScrollPane();
		chattingContentScroll.setBounds(0, 117, 456, 553);
		roomPanel.add(chattingContentScroll);
		
		chattingContent = new JTextArea();
		chattingContentScroll.setViewportView(chattingContent);
		chattingContent.setEditable(false);
		
		sendMessageField = new JTextField();
		sendMessageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
					sendRequest(requestDto);
					sendMessageField.setText("");
				}
			}
		});
		sendMessageField.setBounds(0, 670, 376, 83);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);
		
		JButton sendButton = new JButton("전송");
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
				sendRequest(requestDto);
				sendMessageField.setText("");
			}
		});
		sendButton.setBounds(377, 670, 79, 83);
		roomPanel.add(sendButton);
		
		
		
	}
	
	private void sendRequest(RequestDto<?> requestDto) {
		String reqJson = gson.toJson(requestDto);
		OutputStream outputStream = null;
		PrintWriter printWriter = null;
		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(reqJson);
			System.out.println("클라이언트 -> 서버:" + reqJson);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
				
	}
}
