package com.example.simpletodojenkins;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.io.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_positon";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(),Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error Reading Items", e);
        }
    }

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter ItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnADD);
        etItem = findViewById(R.id.etItems);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        ItemsAdapter.OnClickListener OnClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single Click at Position" + position);
                //Create New Activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                //Pass the Data being Edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                //Display the Activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };


        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
                //Delete the item from the model
                items.remove(position);
                //Notify the adapter
                ItemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was Removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };
        final ItemsAdapter itemsAdapter = new ItemsAdapter(items, onLongClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = etItem.getText().toString();
                //Add item to the model
                items.add(todoItem);
                //Notify adapter that an item is inserted
                itemsAdapter.notifyItemInserted(items.size()-1);
                etItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was Added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }
    //handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            //Retrieve the updated text values
            assert data != null;
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            //extract the original of the edited item from the position key
            int position = Objects.requireNonNull(data.getExtras()).getInt(KEY_ITEM_POSITION);

            //Update model at the right position with new item text
            items.set(position, itemText);
            //Notify Adapter of Change
            ItemsAdapter.notifyItemChanged(position);
            //Persist the Changes
            saveItems();
            Toast.makeText (getApplicationContext(), "Items updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(),"data.txt");
    }

    //This function will load items by reading every line of the data file
    private void loadItems() {
       try {
           items = new ArrayList<>(FileUtils.readLines(getDataFile(),Charset.defaultCharset()));
       } catch (IOException e) {
           Log.e("MainActivity", "Error Reading Messages", e );
           items = new ArrayList<>();
       }
    }
    //This function saves items by writing them into the data file
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch(IOException e){
            Log.e("MainActivity", "Error Writing Items", e );
        }
    }
}