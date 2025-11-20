package hibernate_spring_bank_app.helper;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TransactionHelper {

    private final SessionFactory sessionFactory;

    @Autowired
    public TransactionHelper(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void executeInTransaction(Consumer<Session> action) {
        Transaction transaction = null;
        try(Session session = sessionFactory.getCurrentSession()){
            transaction = session.getTransaction();
            transaction.begin();
            action.accept(session);
            session.getTransaction().commit();
        }
        catch(RuntimeException ex){
            if(transaction!=null){
                transaction.rollback();
            }
            throw ex;
        }
    }

    public<T> T executeInTransaction(Function<Session,T> action) {
        Transaction transaction = null;
        try(Session session = sessionFactory.getCurrentSession()){
            transaction = session.getTransaction();
            transaction.begin();
            var result = action.apply(session);
            session.getTransaction().commit();
            return result;
        }
        catch(RuntimeException ex){
            if(transaction!=null){
               transaction.rollback();
            }
            throw ex;
        }
    }
}
