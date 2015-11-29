package de.markusfisch.android.shadereditor.fragment;

import de.markusfisch.android.shadereditor.app.ShaderEditorApplication;
import de.markusfisch.android.shadereditor.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class TextureDetailsFragment extends Fragment
{
	private InputMethodManager imm;
	private SeekBar sizeBarView;
	private TextView sizeView;
	private EditText nameView;

	@Override
	public void onCreate( Bundle state )
	{
		super.onCreate( state );

		setHasOptionsMenu( true );
	}

	@Override
	public View onCreateView(
		LayoutInflater inflater,
		ViewGroup container,
		Bundle state )
	{
		Activity activity;
		View view;

		if( (activity = getActivity()) == null )
			return null;

		activity.setTitle( R.string.texture_properties );

		if( (view = inflater.inflate(
				R.layout.fragment_texture_details,
				container,
				false )) == null ||
			(sizeBarView = (SeekBar)view.findViewById(
				R.id.size_bar )) == null ||
			(sizeView = (TextView)view.findViewById(
				R.id.size )) == null ||
			(nameView = (EditText)view.findViewById(
				R.id.name )) == null )
		{
			activity.finish();
			return null;
		}

		imm = (InputMethodManager)activity.getSystemService(
			Context.INPUT_METHOD_SERVICE );

		initSizeView();

		return view;
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		inflater.inflate(
			R.menu.fragment_texture_details,
			menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case R.id.save:
				saveTexture();
				return true;
			default:
				return super.onOptionsItemSelected( item );
		}
	}

	private void initSizeView()
	{
		setSizeView( sizeBarView.getProgress() );
		sizeBarView.setOnSeekBarChangeListener(
			new SeekBar.OnSeekBarChangeListener()
			{
				int progress = 0;

				@Override
				public void onProgressChanged(
					SeekBar seekBar,
					int progresValue,
					boolean fromUser )
				{
					setSizeView( progresValue );
				}

				@Override
				public void onStartTrackingTouch(
					SeekBar seekBar )
				{
				}

				@Override
				public void onStopTrackingTouch(
					SeekBar seekBar )
				{
				}
			} );
	}

	private void setSizeView( int power )
	{
		int size = getPower( power );

		sizeView.setText( String.format(
			"%d x %d",
			size,
			size ) );
	}

	private void saveTexture()
	{
		Bitmap bitmap = CropImageFragment.bitmap;
		Rect rect = CropImageFragment.rect;

		if( bitmap == null ||
			rect == null )
			return;

		String name = nameView.getText().toString();

		if( name == null ||
			name.trim().length() < 1 )
		{
			toast(
				getActivity(),
				R.string.missing_name );

			return;
		}
		else if( !name.matches( "[a-zA-Z0-9]+" ) )
		{
			toast(
				getActivity(),
				R.string.invalid_texture_name );

			return;
		}

		try
		{
			bitmap = Bitmap.createBitmap(
				bitmap,
				rect.left,
				rect.top,
				rect.width(),
				rect.height() );
		}
		catch( IllegalArgumentException e )
		{
			toast(
				getActivity(),
				R.string.illegal_rectangle );

			return;
		}

		int size = getPower( sizeBarView.getProgress() );

		if( ShaderEditorApplication
			.dataSource
			.insertTexture(
				name,
				Bitmap.createScaledBitmap(
					bitmap,
					size,
					size,
					true ) ) < 1 )
		{
			toast(
				getActivity(),
				R.string.name_already_taken );

			return;
		}

		if( !isAdded() )
			return;

		imm.hideSoftInputFromWindow(
			nameView.getWindowToken(),
			0 );

		getActivity().finish();
	}

	private static int getPower( int power )
	{
		return 1 << power;
	}

	private static void toast( Activity activity, int id )
	{
		if( activity == null )
			return;

		Toast.makeText(
			activity,
			id,
			Toast.LENGTH_SHORT ).show();
	}
}
