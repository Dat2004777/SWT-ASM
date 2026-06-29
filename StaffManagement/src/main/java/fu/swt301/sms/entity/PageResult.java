package fu.swt301.sms.entity;

import java.util.List;

public class PageResult<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;

    public PageResult(List<T> items, int currentPage, int pageSize, int totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    }

    public boolean isHasPrevious() {
        return currentPage > 1;
    }

    public boolean isHasNext() {
        return currentPage < getTotalPages();
    }
}
