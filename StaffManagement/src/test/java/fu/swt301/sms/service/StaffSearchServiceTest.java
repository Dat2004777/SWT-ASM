package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.PageResult;
import fu.swt301.sms.entity.Staff;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StaffSearchService} (FR-06 search and pagination).
 * <p>
 * {@link StaffDAO} is mocked with Mockito so no database is required.
 * Pagination inputs use equivalence partitioning: valid page, non-positive
 * page, and page greater than the maximum page.
 */
class StaffSearchServiceTest {

    private StaffDAO staffDAO;
    private StaffSearchService service;

    @BeforeEach
    void setUp() {
        staffDAO = mock(StaffDAO.class);
        service = new StaffSearchService(staffDAO);
    }

    private static Staff staff(int id, String fullName) {
        Staff staff = new Staff();
        staff.setStaffID(id);
        staff.setFullName(fullName);
        return staff;
    }

    // ---------- search ----------

    @Test
    void search_matchingName_returnsMatchingStaff() {
        Staff expected = staff(1, "Nguyen Van A");
        PageResult<Staff> daoResult = new PageResult<>(List.of(expected), 1, 10, 1);
        when(staffDAO.search("Nguyen", null, null, 1, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search("Nguyen", null, null, 1);

        assertSame(daoResult, result);
        assertEquals(1, result.getItems().size());
        assertEquals("Nguyen Van A", result.getItems().get(0).getFullName());
    }

    @Test
    void search_noMatchingStaff_returnsEmptyPage() {
        PageResult<Staff> daoResult = new PageResult<>(List.of(), 1, 10, 0);
        when(staffDAO.search("NotFound", null, null, 1, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search("NotFound", null, null, 1);

        assertSame(daoResult, result);
        assertEquals(0, result.getItems().size());
        assertEquals(0, result.getTotalItems());
    }

    // ---------- pagination equivalence partitions ----------

    @Test
    void search_validPage_keepsRequestedPage() {
        PageResult<Staff> daoResult = new PageResult<>(List.of(staff(11, "Staff 11")), 2, 10, 25);
        when(staffDAO.search(null, null, null, 2, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search(null, null, null, 2);

        assertEquals(2, result.getCurrentPage());
        verify(staffDAO).search(null, null, null, 2, 10);
    }

    @Test
    void search_negativePage_normalizesToFirstPage() {
        PageResult<Staff> daoResult = new PageResult<>(List.of(staff(1, "Staff 1")), 1, 10, 25);
        when(staffDAO.search(null, null, null, 1, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search(null, null, null, -5);

        assertEquals(1, result.getCurrentPage());
        verify(staffDAO).search(null, null, null, 1, 10);
    }

    @Test
    void search_zeroPage_normalizesToFirstPage() {
        PageResult<Staff> daoResult = new PageResult<>(List.of(staff(1, "Staff 1")), 1, 10, 25);
        when(staffDAO.search(null, null, null, 1, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search(null, null, null, 0);

        assertEquals(1, result.getCurrentPage());
        verify(staffDAO).search(null, null, null, 1, 10);
    }

    @Test
    void search_pageGreaterThanMaximum_returnsLastPage() {
        // 25 records at 10 records/page means page 3 is the maximum.
        // StaffDAO applies the upper bound after counting matching records.
        PageResult<Staff> daoResult = new PageResult<>(List.of(staff(21, "Staff 21")), 3, 10, 25);
        when(staffDAO.search(null, null, null, 99, 10)).thenReturn(daoResult);

        PageResult<Staff> result = service.search(null, null, null, 99);

        assertEquals(3, result.getCurrentPage());
        assertEquals(3, result.getTotalPages());
        verify(staffDAO).search(null, null, null, 99, 10);
    }
}
