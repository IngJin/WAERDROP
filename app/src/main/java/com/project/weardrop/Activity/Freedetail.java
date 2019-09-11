package com.project.weardrop.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.weardrop.R;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.project.weardrop.Activity.Board.EXTRA_FILEPATH;
import static com.project.weardrop.Activity.Board.EXTRA_ID;
import static com.project.weardrop.Activity.Board.EXTRA_TITLE;
import static com.project.weardrop.Activity.Board.EXTRA_WRITEDATE;
import static com.project.weardrop.Activity.Board.EXTRA_WRITER;
import static com.project.weardrop.Activity.Board.EXTRA_CONTENT;

public class Freedetail extends AppCompatActivity implements Runnable {
    String id;
    String filepath;
    ImageView imageView;
    private ArrayList<Freelistitem> mFreeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freedetail);
        mFreeList = new ArrayList<>();

        // bottom) 버튼 클릭시 사용되는 리스너를 구현
        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        // 어떤 메뉴 아이템이 터치되었는지 확인
                        switch (item.getItemId()) {
                            case R.id.menuitem_bottombar_home:
                                Toast.makeText(getApplicationContext(), "홈버튼 클릭", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.menuitem_bottombar_update:
                                final AlertDialog.Builder builder1 = new AlertDialog.Builder(bottomNavigationView.getContext());
                                builder1.setTitle("수정");
                                builder1.setMessage("해당 항목을 수정하시겠습니까?");
                                builder1.setPositiveButton("예",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //상세화면에 있는 데이터를 수정화면으로 넘기기
                                                Intent intent = new Intent(Freedetail.this, UpdateActivity.class);
                                                TextView textViewId = findViewById(R.id.id);
                                                TextView textViewTitle = findViewById(R.id.title_detail);
                                                ImageView imageView = findViewById(R.id.detail_image_view);
                                                TextView textViewContent = findViewById(R.id.content_detail);
                                                TextView textViewWriter = findViewById(R.id.writer_detail);
                                                TextView textViewWritedate = findViewById(R.id.writedate_detail);

                                                intent.putExtra("id", textViewId.getText().toString() );
                                                intent.putExtra("title", textViewTitle.getText().toString());
                                                intent.putExtra("filepath", filepath);
                                                intent.putExtra("content", textViewContent.getText().toString());
                                                intent.putExtra("writer", textViewWriter.getText().toString());
                                                intent.putExtra("writedate", textViewWritedate.getText().toString());
                                                startActivity(intent);
                                            }
                                        });
                                builder1.setNegativeButton("아니오",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder1.show();
                                return true;

                            case R.id.menuitem_bottombar_delete:
                                //삭제 알림창 띄우기
                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(bottomNavigationView.getContext());
                                builder2.setTitle("삭제");
                                builder2.setMessage("해당 항목을 삭제하시겠습니까?");
                                builder2.setPositiveButton("예",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                 Thread th = new Thread(Freedetail.this);
                                                 th.start();
                                                 finish();
                                            }
                                        });
                                                                                                                                                                                                  builder2.setNegativeButton("아니오",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder2.show();
                                return true;
                        }
                        return false;
                }
                });

        //서버에서 데이터 가져오기
        final Intent intent = getIntent();

        id = intent.getStringExtra(EXTRA_ID);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String writer = intent.getStringExtra(EXTRA_WRITER);
        String writedate = intent.getStringExtra(EXTRA_WRITEDATE);
        String content = intent.getStringExtra(EXTRA_CONTENT);
        filepath = intent.getStringExtra(EXTRA_FILEPATH);


        ImageView mimageView = findViewById(R.id.detail_image_view);
        TextView textViewId = findViewById(R.id.id);
        TextView textViewTitle = findViewById(R.id.title_detail);
        TextView textViewWriter = findViewById(R.id.writer_detail);
        TextView textViewWritedate = findViewById(R.id.writedate_detail);
        TextView textViewContent = findViewById(R.id.content_detail);

        textViewId.setText(id);
        textViewTitle.setText(title);
        textViewWriter.setText(writer);
        textViewWritedate.setText(writedate);
        textViewContent.setText(content);

        textViewId.setVisibility(View.INVISIBLE);       //레이아웃에서 id값(textview) 안보이게 하기

        Glide.with(this).load("http://112.164.58.7:80/weardrop/resources" + filepath).into(mimageView);
    }

    @Override
    public void run() {
            String url = "http://192.168.0.21:80/teamproject/anddelete.com" ;

            try {
                // NmaeValuePair 변수명과 값을 함께 저장하는 객체
                HttpClient http = new DefaultHttpClient();

                ArrayList<NameValuePair> postData = new ArrayList<>();
                // post 방식으로 전달할 값들
               postData.add(new BasicNameValuePair("id", id));
                // URI encoding이 필요한 한글, 특수문자 값들 인코딩
                UrlEncodedFormEntity request = new UrlEncodedFormEntity(postData, "utf-8");
                HttpPost httpPost = new HttpPost(url);
                // http 에 인코딩된 값 세팅
                httpPost.setEntity(request);
                // post 방식으로 전달하고 응답은 response에 저장
                HttpResponse response = http.execute(httpPost);
                // response text를 String으로 변환
                String body = EntityUtils.toString(response.getEntity());
                // String 을 JSON으로...
                JSONObject obj = new JSONObject(body);
                final String message = obj.getString("message");
                // 백그라운드 스레드에서 메인 UI를 변경하고자 하는경우 사용
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (message != null) {
                            Intent intent = new Intent(Freedetail.this, Board.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            Toast.makeText(Freedetail.this,"게시글이 삭제되었습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
