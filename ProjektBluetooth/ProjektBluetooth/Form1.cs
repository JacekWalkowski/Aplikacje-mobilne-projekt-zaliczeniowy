using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.IO;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Drawing.Imaging;
using InTheHand.Net.Bluetooth;
using InTheHand.Net;
using InTheHand.Net.Sockets;

namespace ProjektBluetooth
{
    public partial class Form1 : Form
    {
        private BluetoothClient bluetoothClient;
        private BluetoothDeviceInfo[] array;
        private Bitmap bitmap;
        private Graphics graphics;
        private Thread t;
        private delegate void Update();
        private Update myDelegate;
        private Stream stream;
        private StringBuilder str = new StringBuilder();
        private void Thread()
        {
            while (true)
            {
                byte[] bytes = new byte[1];
                Console.WriteLine("ILE BAJTOW: " + bluetoothClient.Available);                
                stream.Read(bytes, 0, 1);
                if (bytes[0] > 249)
                {
                    switch (bytes[0])
                    {
                        case 250:
                            SendKeys.SendWait("{F6}");
                            break;

                        case 251:
                            SendKeys.SendWait("+{F1}");
                            break;

                        case 252:
                            SendKeys.SendWait("+{F2}");
                            break;
                            
                        case 253:
                            SendKeys.SendWait("+{F3}");
                            break;
                        
                        case 254:
                            SendKeys.SendWait("+{F4}");
                            break;

                        case 255:
                            SendKeys.SendWait("+{F5}");
                            break;
                    }



                    continue;
                } 
                if (bytes[0] != 35)
                {
                    str.Append(System.Text.Encoding.UTF8.GetString(bytes));
                }
                else
                {
                    UpdateUI();
                    //Console.WriteLine("ODEBRANO         " + str.ToString());
                    str.Clear();
                }
            }

                
        }

        private void UpdateUI()
        {
            if (this.InvokeRequired)
            {
                Invoke(myDelegate);
            }
            else
            {
                listBox2.Items.Add(bluetoothClient.RemoteMachineName+":   "+str.ToString());
                //graphics.CopyFromScreen(0, 0, 0, 0, bitmap.Size);
                
                //bitmap.Save("zrzut", ImageFormat.Png);
                //pictureBox1.Image = bitmap;
            }
        }
        public Form1()
        {
            InitializeComponent();
            BluetoothListener listener = new BluetoothListener(new Guid("{00112233-4455-6677-8899-aabbccddeeff}"));
            Thread listeningThread = new Thread(new ThreadStart(listening));
            listeningThread.Start();

            bitmap = new Bitmap(Screen.PrimaryScreen.Bounds.Width, Screen.PrimaryScreen.Bounds.Height);
            graphics = Graphics.FromImage(bitmap as Image);
            //pictureBox1.SizeMode = PictureBoxSizeMode.StretchImage;
            myDelegate = new Update(UpdateUI);
            t = new Thread(Thread);
            
            bluetoothClient = new BluetoothClient();
            bluetoothClient.InquiryLength = TimeSpan.FromSeconds(2);
            //SendKeys.SendWait("{F6}");
            

            //System.Diagnostics.Process[] procesy = System.Diagnostics.Process.GetProcesses();

            //for (int i = 0; i < procesy.Length; ++i)
            //{
            //    Console.WriteLine("PROCES: " + procesy[i].ProcessName);
            //}
            
        }

        void listening()
        {
            try
            {
                bool run = true;
                BluetoothListener btl = new BluetoothListener(new Guid("{00112233-4455-6677-8899-aabbccddeeff}"));
                btl.Start();
                while (run)
                {
                    //BluetoothClient btReceiver = btl.AcceptBluetoothClient();
                    bluetoothClient = btl.AcceptBluetoothClient();

                    if (bluetoothClient.Connected == true)
                    {
                        toolStripStatusLabel1.Text = "Połączono z " + bluetoothClient.RemoteMachineName;
                        stream = bluetoothClient.GetStream();
                        t.Start();
                        break;
                    } 
                }
                
            }
            catch (Exception ex) { MessageBox.Show(ex.Message); }
        }

        private void Form1_Load(object sender, EventArgs e)
        {
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            t.Abort();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            List<string> lista = new List<string>();
            array = bluetoothClient.DiscoverDevices();            
            for (int i = 0; i < array.Length; ++i)
            {
                lista.Add(array[i].DeviceName);
            }
            listBox1.DataSource = lista;            
            //BluetoothAddress adress = array[0].DeviceAddress;
        }

        private void button2_Click(object sender, EventArgs e)
        {            
            toolStripStatusLabel1.Text = "Niepołączony"; 
            int i = listBox1.SelectedIndex;            
            Guid guid = new Guid("{00112233-4455-6677-8899-aabbccddeeff}");
            BluetoothEndPoint ep = new BluetoothEndPoint(array[i].DeviceAddress, guid);
            if (!bluetoothClient.Connected)
            {
                bluetoothClient.Connect(ep);
                stream = bluetoothClient.GetStream();
                stream.WriteTimeout = 5000;
                t.Start();
            } 
            if (bluetoothClient.Connected) toolStripStatusLabel1.Text = "Połączono z " + bluetoothClient.RemoteMachineName;        
        }

        private void timer1_Tick(object sender, EventArgs e)
        {

        }

        private void button3_Click(object sender, EventArgs e)
        {
            graphics.CopyFromScreen(0, 0, 0, 0, bitmap.Size);
            //bitmap.Save("zrzut", ImageFormat.Png);
            //bitmap.Save(
        }

        private void button4_Click(object sender, EventArgs e)
        {
            //if (bluetoothClient.Connected && bitmap != null)
            //{
            //    ImageConverter imageConverter = new ImageConverter();
            //    byte[] bufor = (byte[])imageConverter.ConvertTo(bitmap, typeof(byte[]));
            //    Console.WriteLine("ROZMIAR " + bufor.Length);

            //    byte[] opcja = new byte[2];
            //    opcja[0] = 1;

            //    byte[] intBytes = BitConverter.GetBytes(bufor.Length);
            //    if (BitConverter.IsLittleEndian)
            //        Array.Reverse(intBytes);
            //    byte[] result = intBytes;


            //    Console.WriteLine("ile bajtow: " + result.Length);
            //    stream.Write(opcja, 0, 1);
            //    stream.Write(result, 0, 4);
            //    stream.Write(bufor, 0, bufor.Length);
            //}

            if (bluetoothClient.Connected)
            {
                byte[] opcja = new byte[1];
                opcja[0] = 2;
                String str = textBox1.Text;
                byte[] bytes = new byte[str.Length * sizeof(char)];
                System.Buffer.BlockCopy(str.ToCharArray(), 0, bytes, 0, bytes.Length);
                stream.Write(opcja, 0, 1);                
                stream.Write(bytes, 0, bytes.Length);
                listBox2.Items.Add("Ja: " + str);
                textBox1.Clear();
            }
        }

        private void timer1_Tick_1(object sender, EventArgs e)
        {

        }

        private void toolStripStatusLabel1_Click(object sender, EventArgs e)
        {

        }
    }
}
