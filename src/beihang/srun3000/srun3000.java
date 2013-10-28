package beihang.srun3000;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import beihang.srun3000.*;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class srun3000 extends Activity {
	private TextView tv;
	private EditText user;
	private EditText pass;
	private Button btnlogin;
	private CheckBox cbsave;
	private String uid=null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv=(TextView)findViewById(R.id.tv);
        user=(EditText)findViewById(R.id.txtUsername);
        pass=(EditText)findViewById(R.id.txtPass);
        btnlogin=(Button)findViewById(R.id.btnlogin);
        cbsave=(CheckBox)findViewById(R.id.cbsave);
        btnlogin.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		if(getLocalMacAddress()==null){
                	Toast.makeText(srun3000.this, "WIFI not enabled.", Toast.LENGTH_LONG);
                	return;
                }
        		try{
        		if(uid==null){
                	String time=System.currentTimeMillis()/1000/60+"";
                	String result="";
                	for(int i=0;i<2;i++){
                		HttpPost hp=new HttpPost("http://202.112.136.131:3333/cgi-bin/do_login");
                		hp.addHeader("Content-Type", "application/x-www-form-urlencoded");
                		hp.addHeader("User-Agent", "my session");
                		String str="username="+user.getText().toString()+"&password=";//&drop=0&type=1&n=100";
                		str+=URLEncoder.encode(encrypt(pass.getText().toString(),time),"UTF-8");
                		str+="&drop=0&type=2&n=16&mac=";
                		str+=URLEncoder.encode(encrypt(getLocalMacAddress(),time),"UTF-8");
                		tv.setText(str);
                		hp.setEntity(new StringEntity(str));
                		HttpResponse response = new DefaultHttpClient().execute(hp);
                		int rc=response.getStatusLine().getStatusCode();
                		if(rc == 200){
                			result = EntityUtils.toString(response.getEntity());
                			String[] res=result.split("@");
                			if(res.length>1){
                				int nt=Integer.parseInt(res[1]);
                				time=nt/60+"";
                			}
                			else
                				break;
                		}
                		else {
            			result=rc+"";
                		}
            		}
                	if(result.split(",").length>1){
                		uid=result.split(",")[0];
                		btnlogin.setText("Logout");
                		tv.setText("Login succeed.");
                		SharedPreferences settings= getSharedPreferences("srun3000", 0); 
                		SharedPreferences.Editor editor = settings.edit();  
                		editor.putString("username",user.getText().toString());
                		if(cbsave.isChecked())editor.putString("password",pass.getText().toString());
                		else
                			editor.putString("password","");
                		
                		editor.commit();  
                	}
                	else
                		tv.setText(result.split("@")[0]);
        		}
        		else
        		{
        			HttpPost hp=new HttpPost("http://202.112.136.131:3333/cgi-bin/do_logout");
            		hp.addHeader("Content-Type", "application/x-www-form-urlencoded");
            		hp.addHeader("User-Agent", "my session");
            		String str="uid="+uid;
            		hp.setEntity(new StringEntity(str));
            		HttpResponse response = new DefaultHttpClient().execute(hp);
            		int rc=response.getStatusLine().getStatusCode();
            		if(rc == 200){
            			tv.setText(EntityUtils.toString(response.getEntity()));
            			btnlogin.setText("Login");
            			uid=null;
            		}
        		}
                }
                catch(Exception ex){
                	Toast.makeText(srun3000.this,ex.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
        	}
        });
        SharedPreferences settings = getSharedPreferences("srun3000", 0);
        user.setText(settings.getString("username", ""));
        pass.setText(settings.getString("password", ""));
        if(pass.getText().toString()!="")cbsave.setChecked(true);
    }
    public String encrypt(String str,String key)
    {
    	//tv.setText(str+" "+key);
    	String res="";
    	for(int i=0;i<str.length();i++)
    	{
    		int ki=(int)(key.charAt(key.length()-i%key.length()-1));
    		int pi=(int)(str.charAt(i));
    		ki=ki^pi;
    		res+=buildkey(ki,i%2);
    	}
    	return res;
    }
public String buildkey(int num, int reverse)
{
    String ret = "";
    int _low = num & 0x0f;

    int _high = num >> 4;
    _high = _high & 0x0f;

    if (reverse == 0)
    {
        char temp1 = (char)(_low + 0x36);
        char temp2 = (char)(_high + 0x63);

        ret = temp1+""+temp2;
    }
    else
    {
        char temp1 = (char)(_high + 0x63);
        char temp2 = (char)(_low + 0x36);

        ret = temp1+""+temp2;
    }
    return ret;
}
    public String getLocalMacAddress() { 
    	try{
    	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress()==null?"00:11:22:33:44:55":info.getMacAddress();
    	}
    	catch(Exception ex)
    	{
    		return "00:11:22:33:44:55";
        }
    }  
}