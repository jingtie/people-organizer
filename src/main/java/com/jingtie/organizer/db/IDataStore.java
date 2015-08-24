package com.jingtie.organizer.db;

import com.jingtie.organizer.dao.FamilyDao;
import com.jingtie.organizer.dao.PersonDao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by jingtie on 8/22/15.
 */
public interface IDataStore {

    PersonDao createPerson(String name, String email) throws SQLException;

    PersonDao getPerson(int id) throws SQLException;

    FamilyDao createFamily(String name, List<Integer> memberIds) throws SQLException;

    FamilyDao getFamily(int id) throws SQLException;

    List<PersonDao> getMembers(int familyId) throws SQLException;

    void putPersonInFamily(int personId, int familyId) throws SQLException;

    void deletePerson(int personId) throws SQLException;

}