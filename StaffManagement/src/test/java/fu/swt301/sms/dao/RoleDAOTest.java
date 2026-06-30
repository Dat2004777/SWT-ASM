/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.utils.DBUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleDAO to achieve maximum line and branch coverage (>= 90%).
 * Uses Mockito static mocking to isolate connection framework.
 *
 * * @author dat20
 */
public class RoleDAOTest {

    private RoleDAO roleDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DBUtils> mockedDbUtils;

    @BeforeEach
    public void setUp() throws Exception {
        roleDAO = new RoleDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDbUtils = mockStatic(DBUtils.class);
        mockedDbUtils.when(DBUtils::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    public void tearDown() {
        mockedDbUtils.close();
    }

    @Test
    @DisplayName("UT_ROLE_001: Lấy danh sách vai trò thành công (Bao phủ luồng đọc dữ liệu chính)")
    public void testGetAllRoles_Success_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("Role_ID")).thenReturn(1);
        when(mockResultSet.getString("Role_Name")).thenReturn("Admin");

        List<Role> roleList = roleDAO.getAllRoles();

        assertNotNull(roleList, "Danh sách vai trò trả về không được phép null");
        assertEquals(2, roleList.size(), "Số lượng phần tử trích xuất từ ResultSet phải bằng 2");

        Role firstRole = roleList.get(0);
        assertEquals(1, firstRole.getRoleID());
        assertEquals("Admin", firstRole.getRoleName());

        verify(mockConnection, times(1)).prepareStatement("SELECT * FROM Role");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    @DisplayName("UT_ROLE_002: ResultSet trống không có dữ liệu (Bao phủ nhánh danh sách rỗng)")
    public void testGetAllRoles_Empty_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(false);

        List<Role> roleList = roleDAO.getAllRoles();

        assertNotNull(roleList);
        assertTrue(roleList.isEmpty(), "Danh sách trả về phải rỗng khi database không có bản ghi");
    }

    @Test
    @DisplayName("UT_ROLE_003: Xử lý ngoại lệ kết nối DB (Bao phủ khối lệnh catch Exception)")
    public void testGetAllRoles_Exception_Coverage() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated Database Error"));

        List<Role> roleList = assertDoesNotThrow(() -> roleDAO.getAllRoles(),
                "Hàm getAllRoles() phải tự catch ngoại lệ bên trong, không được ném ra ngoài");

        assertNotNull(roleList);
        assertTrue(roleList.isEmpty(), "Khi dính lỗi kết nối, hàm phải trả về danh sách trống khởi tạo ban đầu");
    }
}
