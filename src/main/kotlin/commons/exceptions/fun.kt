package commons.exceptions

import java.sql.SQLException

/**
 * Returns an [SQLException] with the message "The connection is closed".
 *
 * @return an [SQLException]
 */
fun connectionClosedException() = SQLException("The connection is closed")