package com.vanderbilt.people.finder;

import com.vanderbilt.people.finder.Provider.Constants;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainCursorAdapter extends CursorAdapter
{
	public MainCursorAdapter(Context context)
	{
		super(context, null, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) 
	{
		TextView textName = (TextView)view.findViewById(R.id.text_name);
		TextView textStatus = (TextView)view.findViewById(R.id.text_status);
		TextView textAddress = (TextView)view.findViewById(R.id.text_address);
		
		textName.setText(cursor.getString(cursor.getColumnIndex(Constants.NAME)));
		textStatus.setText(cursor.getString(cursor.getColumnIndex(Constants.STATUS)));
		textAddress.setText(cursor.getString(cursor.getColumnIndex(Constants.ADDRESS)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.main_row, null);
	}
}
