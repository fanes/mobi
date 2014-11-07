package com.example.mycamera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
public class PictrueView extends Activity{
private List<String> phoList;
private static ImageAdapter myImageAdapter;
public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_view);
        phoList = readFiles("sdcard/fatalityUpload/Thumbnail_fatality/");
        GridView gridview=(GridView)findViewById(R.id.picGridView);//�ҵ�main.xml�ж���gridview ��id
        myImageAdapter = new ImageAdapter(this,phoList);
        gridview.setAdapter(myImageAdapter);//����ImageAdapter.java
        gridview.setOnItemClickListener(new OnItemClickListener(){//�����¼�
         public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//         myImageAdapter.getItemId(position);
         String fileArr[] = phoList.get(position).toString().split("/");
         String newFilePath = "sdcard/fatalityUpload/fatality/"+fileArr[fileArr.length-1];
         Intent intent = new Intent();
         Bundle extras = new Bundle();
         extras.putString("prePath", newFilePath);
         intent.putExtras(extras);
         intent.setClass(PictrueView.this,SubmitPhoto.class);
         finish();
     startActivity(intent);
//         Toast.makeText(PictrueView.this, ""+position,Toast.LENGTH_SHORT).show();//��ʾ��Ϣ;
         }
        });
    }
@SuppressWarnings("unchecked")
private static List<String> readFiles(String path) {
List picFiles = new ArrayList();
// �����ļ�����
File dir = new File(path);
// �õ����ļ����������ļ�
File[] files = dir.listFiles();
if (files != null) {
for (int i = 0; i < files.length; i++) {
String fileName = files[i].getName();
// �������к�׺Ϊ.jpg���ļ�
if (fileName.lastIndexOf(".") > 0
&& fileName.substring(fileName.lastIndexOf(".") + 1,
fileName.length()).equals("jpg")) {
picFiles.add(files[i].getPath());
}
}
}
return picFiles;
}
}

