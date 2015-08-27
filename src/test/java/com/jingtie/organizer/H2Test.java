package com.jingtie.organizer;

import com.jingtie.organizer.dao.FamilyDao;
import com.jingtie.organizer.dao.PersonDao;
import com.jingtie.organizer.db.H2Impl;
import com.jingtie.organizer.db.IDataStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jingtie on 8/22/15.
 */
public class H2Test {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void test()
    {
        try
        {
            IDataStore dataStore = H2Impl.getInstance();
            PersonDao firstPerson = createPerson();

            List<Integer> memberIds = new LinkedList<>();
            memberIds.add(firstPerson.getId());

            FamilyDao firstFamily = createFamily(memberIds);

            PersonDao secondPerson = createPerson();
            memberIds.add(secondPerson.getId());
            List<Integer> familyIds = new LinkedList<>();
            familyIds.add(firstFamily.getId());
            dataStore.putPersonInFamilies(secondPerson.getId(), familyIds);

            List<PersonDao> members = dataStore.getMembers(firstFamily.getId());
            assert members.size() == 2;
            int member0 = members.get(0).getId();
            int member1 = members.get(1).getId();
            if(member0 == firstPerson.getId())
            {
                assert member1 == secondPerson.getId();
            }
            else
            {
                assert member0 == secondPerson.getId();
                assert member1 == firstPerson.getId();
            }

            List<PersonDao> personList = dataStore.listPerson();
            assert personList.size() == 2;

            dataStore.deletePerson(secondPerson.getId());
            members = dataStore.getMembers(firstFamily.getId());
            assert members.size() == 1;
            assert members.get(0).getId() == firstPerson.getId();

            personList = dataStore.listPerson();
            assert personList.size() == 1;

            dataStore.deletePerson(firstPerson.getId());
            personList = dataStore.listPerson();
            assert personList.size() == 0;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public static PersonDao createPerson() throws Exception
    {
        IDataStore dataStore = H2Impl.getInstance();

        String name = "person" + System.currentTimeMillis();
        String email = name + "@jingtie.com";
        PersonDao person = dataStore.createPerson(name, email);

        PersonDao gottenPerson = dataStore.getPerson(person.getId());
        assert gottenPerson.getEmail().equals(person.getEmail());

        return person;
    }

    public static FamilyDao createFamily(List<Integer> members) throws Exception
    {
        IDataStore dataStore = H2Impl.getInstance();

        String name = "family" + System.currentTimeMillis();
        FamilyDao family = dataStore.createFamily(name, members);
        FamilyDao gottenFamily = dataStore.getFamily(family.getId());

        assert gottenFamily.getName().equals(family.getName());
        assert gottenFamily.getId() == family.getId();
        assert gottenFamily.getCreatedTime().equals(family.getCreatedTime());
        assert gottenFamily.getModifiedTime().equals(family.getModifiedTime());

        return family;
    }

}
