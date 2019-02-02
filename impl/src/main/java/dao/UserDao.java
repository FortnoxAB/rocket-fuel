package dao;

import api.User;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;


public interface UserDao {

    @Update("INSERT INTO public.\"user\" " +
            "(email, vendor_id, \"name\") " +
            "VALUES(:user.email, :user.vendorId, :user.name);\n")
    Observable<Integer> insertUser(User user);

    @Query(value = "SELECT * from \"user\" where email = :email", maxLimit = 1)
    Observable<User> getUserByEmail(String email);

    @Query(value = "SELECT * from \"user\" where id = :userId", maxLimit = 1)
    Observable<User> getUserById(long userId);
}
