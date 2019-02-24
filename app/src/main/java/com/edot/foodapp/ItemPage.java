package com.edot.foodapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.edot.models.HelperUtil;
import com.edot.network.HttpGETClient;

import java.util.HashMap;

public class ItemPage extends AppCompatActivity {

    public static final int REQ_CODE = 8;

    private ItemSelectionController controller;
    private String hotelID;
    private String hotelName;
    private String hotelComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_menu_chooser);
        controller = new ItemSelectionController(this);
        Intent intent = getIntent();
        hotelID = (intent == null)? null : intent.getStringExtra(HotelLinearViewModel.ATTR_LIST.get(0));
        hotelName = (intent == null)? null : intent.getStringExtra(HotelLinearViewModel.ATTR_LIST.get(1));
        hotelComment = (intent == null)? null : intent.getStringExtra(HotelLinearViewModel.ATTR_LIST.get(2));

        Log.d(AppConstants.LOG_TAG,"Id : " + hotelID);
        Log.d(AppConstants.LOG_TAG,"Name : " + hotelName);
        Log.d(AppConstants.LOG_TAG,"Comment : "+hotelComment);

        if(hotelID != null)
        {
            hotelID = hotelID.toLowerCase();
        }
        if(hotelName == null || hotelComment == null)
        {
            Toast.makeText(this,R.string.error_404,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    public void navigateToBillPage(final View view)
    {
        Log.d(AppConstants.LOG_TAG,"Proceed button clicked : "+AppConstants.currentLoggedInUserID);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setContentView(R.layout.layout_loading);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return OrderHelper.placeOrder(ItemPage.this,AppConstants.currentLoggedInUserID,
                        hotelID,controller.getListOfSelectedItems(),controller.getTotal());
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);

                if (aBoolean)
                {
                    setContentView(R.layout.booking_success);
                }
                else
                {
                    Toast.makeText(ItemPage.this, R.string.orderFailed, Toast.LENGTH_SHORT).show();
                    recreate();
                }
            }
        }.execute();
    }

    private void showItems(final boolean b1,final boolean b2,final boolean b3)
    {
        new AsyncTask<String,Void,View>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setContentView(R.layout.layout_loading);
            }

            @Override
            protected LinearLayout doInBackground(String... strings) {
                HttpGETClient httpGETClient = new HttpGETClient();
                if (httpGETClient.establishConnection("http://autoiot2019-20.000webhostapp.com/" +
                        "FoodApp/"+strings[0]+".properties")) {
                    HashMap<String, HashMap<String, String>> map = HelperUtil.readProperties(httpGETClient
                                    .getInputStream(), ItemLinearViewModel.FIELD,
                            ItemLinearViewModel.ATTR_LIST);
                    httpGETClient.closeConnection();
                    LinearLayout l = (LinearLayout) new ItemLinearViewModel(ItemPage.this,
                            controller,b1,b2,b3).renderMap(map);
                    return l;
                }
                return null;
            }

            @Override
            protected void onPostExecute(View view) {
                super.onPostExecute(view);
                setContentView(R.layout.activity_item_page);
                controller.setRootView((RelativeLayout)findViewById(R.id.itemPageRootLayout));
                View rootView = findViewById(R.id.itemPageRootLayout);
                TextView textView = findViewById(R.id.itemPagehotelTitleTextView);
                textView.setText(hotelName);
                textView = findViewById(R.id.itemPagehotelCommentTextView);
                textView.setText(hotelComment);
                LinearLayout scrollView = findViewById(R.id.itemViewParentLayout);
                if (view == null)
                {
                    Toast.makeText(ItemPage.this,R.string.somethingWentWrongCommon
                            ,Toast.LENGTH_SHORT).show();
                }
                else {
                    scrollView.addView(view);
                }
            }
        }.execute(hotelID);
    }

    public void onMenu(View view) {
        int id = view.getId();
        showItems(id == R.id.breakfast, id == R.id.lunch, id == R.id.dinner);
    }
}
