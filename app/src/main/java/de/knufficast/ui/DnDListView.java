/*******************************************************************************
 * Copyright 2012 Crazywater
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.knufficast.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

/**
 * A {@link ListView} that supports drag and drop by long-pressing items.
 * 
 * @author crazywater
 * 
 */
public class DnDListView extends ListView {
  private Listener listener;
  private boolean dragging;
  private boolean inside;
  private int dragIndex;
  private int dropIndex;
  private ListAdapter adapter;
  private final FakeAdapter fakeAdapter = new FakeAdapter();

  public DnDListView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    this.setOnDragListener(myDragEventListener);
    this.setOnItemClickListener(myClickListener);
    this.setOnItemLongClickListener(myLongClickListener);
  }
  
  /**
   * Set the listener to be notified upon reorder and remove events.
   */
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Wraps around the real adapter set by {@link #setAdapter}: Displays the
   * reordered items if the user is currently dragging one.
   * 
   * @author crazywater
   * 
   */
  private class FakeAdapter implements WrapperListAdapter {
    @Override
    public boolean areAllItemsEnabled() {
      return adapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
      return adapter.isEnabled(position);
    }

    @Override
    public int getCount() {
      if (dragging && !inside) {
        return adapter.getCount() - 1;
      }
      return adapter.getCount();
    }

    @Override
    public Object getItem(int position) {
      return adapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
      return adapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
      return adapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
      return adapter.getViewTypeCount();
    }

    @Override
    public boolean hasStableIds() {
      return adapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
      return adapter.isEmpty();
    }

    private Set<DataSetObserver> observers = new HashSet<DataSetObserver>();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
      observers.add(observer);
      adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
      observers.remove(observer);
      adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public ListAdapter getWrappedAdapter() {
      return adapter;
    }

    private void notifyObservers() {
      for (DataSetObserver observer : observers) {
        observer.onChanged();
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      int ourPosition = position;
      // pretend to drop at Integer.MAX_VALUE if we're not inside for
      // computations
      int ourDropIndex = inside ? dropIndex : Integer.MAX_VALUE;
      if (dragging) {
        if (position == ourDropIndex) {
          // exactly the dragging item
          ourPosition = dragIndex;
        } else {
          if (ourPosition > ourDropIndex) {
            // shift all items one down
            ourPosition--;
          }
          if (ourPosition >= dragIndex) {
            // shift all items one up
            ourPosition++;
          }
        }
      }
      return adapter.getView(ourPosition, convertView, parent);
    }
  };

  @Override
  public void setAdapter(final ListAdapter adapter) {
    this.adapter = adapter;
    super.setAdapter(fakeAdapter);
  }

  private OnItemLongClickListener myLongClickListener = new OnItemLongClickListener() {
    public boolean onItemLongClick(AdapterView<?> arg0, View v, int position,
        long arg3) {
      dragging = true;
      inside = true;
      dragIndex = position;
      dropIndex = position;
      v.startDrag(null, new DragShadowBuilder(v), null, 0);
      return true;
    }
  };

  private OnItemClickListener myClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
        long arg3) {
      listener.click(position);
    }
  };

  private View.OnDragListener myDragEventListener = new View.OnDragListener() {
    @Override
    public boolean onDrag(View v, DragEvent event) {
      final int action = event.getAction();
      switch(action) {
      case DragEvent.ACTION_DRAG_STARTED:
        fakeAdapter.notifyObservers();
        return true;
      case DragEvent.ACTION_DRAG_ENTERED:
        inside = true;
        fakeAdapter.notifyObservers();
        return true;
      case DragEvent.ACTION_DRAG_LOCATION:
        int x = (int) event.getX();
        int y = (int) event.getY();
        int newDropIndex = pointToPosition(x, y);
        if (newDropIndex == AdapterView.INVALID_POSITION) {
          return false;
        }
        if (dropIndex != newDropIndex) {
          dropIndex = newDropIndex;
          fakeAdapter.notifyObservers();
        }
        return true;
      case DragEvent.ACTION_DRAG_EXITED:
        inside = false;
        fakeAdapter.notifyObservers();
        return true;
      case DragEvent.ACTION_DROP:
        return true;
      case DragEvent.ACTION_DRAG_ENDED:
        dragging = false;
        if (inside) {
          listener.drop(dragIndex, dropIndex);
        } else {
          listener.remove(dragIndex);
        };
        return true;
      default:
        return false;
      }
    };
  };

  /**
   * Interface the listener has to implement.
   * 
   * @author crazywater
   * 
   */
  public interface Listener {
    void drop(int from, int to);

    void click(int which);
    void remove(int which);
  }
}
