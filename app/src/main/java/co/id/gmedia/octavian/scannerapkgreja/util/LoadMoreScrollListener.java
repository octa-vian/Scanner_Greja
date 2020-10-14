package co.id.gmedia.octavian.scannerapkgreja.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public abstract class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

    private int loaded;
    private boolean loading = false;
    private boolean canLoad = true;
    private boolean reverse = false;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        boolean scroll;
        if(reverse){
            scroll = dy < 0;
        }
        else{
            scroll = dy > 0;
        }

        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            if(adapter != null){
                if(canLoad && scroll && !loading && layoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1){
                    //Melakukan load more
                    loading = true;
                    onLoadMore();
                }
            }
        }
        else if(recyclerView.getLayoutManager() instanceof GridLayoutManager){
            LinearLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            if(adapter != null){
                if(canLoad && scroll && !loading && (layoutManager.getItemCount() -
                        layoutManager.getChildCount() <= layoutManager.findFirstVisibleItemPosition())){
                    loading = true;
                    onLoadMore();
                }
            }
        }
        else if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager){
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            if (adapter != null) {
                int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
                int lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);

                /*int[] firstVisibleItems = layoutManager.findFirstVisibleItemPositions(null);
                int pastVisibleItems = 0;
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    pastVisibleItems = firstVisibleItems[0];
                }*/
                if (scroll && canLoad && !loading && layoutManager.getChildCount() + lastVisibleItemPosition
                        >=  layoutManager.getItemCount()) {
                    loading = true;
                    onLoadMore();
                }
            }
        }
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    public void initLoad(){
        loaded = 0;
        this.canLoad = true;
    }

    public void finishLoad(int new_item_count)
    {
        if(new_item_count == 0){
            this.canLoad = false;
        }
        this.loading = false;
        loaded += new_item_count;
    }

    public void failedLoad()
    {
        this.loading = false;
    }

    public int getLoaded() {
        return loaded;
    }

    public abstract void onLoadMore();
}
