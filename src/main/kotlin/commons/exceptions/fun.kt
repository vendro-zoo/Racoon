package commons.exceptions

import java.sql.SQLException

fun connectionClosedException() = SQLException("The connection is closed")