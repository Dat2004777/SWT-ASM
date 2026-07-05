package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.PageResult;
import fu.swt301.sms.entity.Staff;

/**
 * Business-logic layer for staff search and pagination (FR-06).
 */
public class StaffSearchService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final StaffDAO staffDAO;

    public StaffSearchService() {
        this(new StaffDAO());
    }

    public StaffSearchService(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    /**
     * Searches staff and normalizes invalid pagination input.
     * The DAO clamps a page greater than the maximum to the last page after
     * obtaining the matching record count.
     */
    public PageResult<Staff> search(
            String name, Integer staffId, String status, int requestedPage) {
        int page = Math.max(1, requestedPage);
        return staffDAO.search(name, staffId, status, page, DEFAULT_PAGE_SIZE);
    }
}
