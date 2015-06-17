package org.kineticsfoundation.dao.persist;

/**
 * Generic DAO exception
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/27/13
 * Time: 9:26 AM
 */
class DaoException extends RuntimeException {

    DaoException(Throwable t) {
        super(t);
    }

}
