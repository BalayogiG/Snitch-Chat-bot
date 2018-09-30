package snitch.com.snitch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ConversationService myConversationService= new ConversationService(
                "2018-04-02",
                getString(R.string.username),
                getString(R.string.password)
        );
        final TextView conversation=(TextView)findViewById(R.id.conversation);
        final EditText userinput=(EditText)findViewById(R.id.user_input);
        userinput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE)
                {
                    final String inputText=userinput.getText().toString();
                    conversation.append(Html.fromHtml("<p><b>You:</b>"+inputText+"</p>"));
                    userinput.setText("");
                    final MessageRequest request=new MessageRequest.Builder().inputText(inputText).build();
                    myConversationService.message(getString(R.string.workspace),request).enqueue(new ServiceCallback<MessageResponse>() {
                        @Override
                        public void onResponse(MessageResponse response) {
                            final String outputText=response.getText().get(0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    conversation.append(Html.fromHtml("<p><b>Snitch:</b>"+outputText+"</p>"));
                                }
                            });
                            if (response.getIntents().get(0).getIntent().endsWith("RequestQuote")){
                                String quoteURL="https://apis.forismatic.com/api/1.0/"+
                                        "?method=getQuote&format=text&lang=en";
                                Fuel.get(quoteURL).responseString(new Handler<String>() {
                                    @Override
                                    public void success(Request request, Response response, String s) {
                                        conversation.append("<p><b>Snitch:</b>"+s+"</p>");
                                    }

                                    @Override
                                    public void failure(Request request, Response response, FuelError fuelError) {
                                        conversation.append("<p><b>Snitch:sorry no quotes...</b></p>");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            conversation.append("<p><b>Snitch:Sorry can't understand you...</b></p>");
                        }
                    });
                }
                return false;
            }
        });
    }
}
