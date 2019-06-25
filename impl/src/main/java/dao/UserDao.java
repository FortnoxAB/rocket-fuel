package dao;

import api.User;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;


public interface UserDao {
    @Update("INSERT INTO \"user\" " +
            "(email, \"name\", picture) " +
            "VALUES(:user.email, :user.name, :user.picture);\n")
    Observable<Integer> insertUser(User user);

    @Query(value = "SELECT * from \"user\" where email = :email LIMIT 1")
    Observable<User> getUserByEmail(String email);

    @Query(value = "SELECT * from \"user\" where id = :userId LIMIT 1")
    Observable<User> getUserById(long userId);
}
