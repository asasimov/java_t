package ru.stqa.t.addressbook.tests;

import com.thoughtworks.xstream.XStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.stqa.t.addressbook.model.ContactData;
import ru.stqa.t.addressbook.model.Contacts;
import ru.stqa.t.addressbook.model.GroupData;
import ru.stqa.t.addressbook.model.Groups;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class ContactCreationTests extends TestBase {

    @DataProvider
    public Iterator<Object[]> validContactsFromXml() throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(app.getProperty("path.contacts.data"))))) {
            String xml = "";
            String line = reader.readLine();
            while (line != null) {
                xml += line;
                line = reader.readLine();
            }

            XStream xstream = new XStream();
            xstream.processAnnotations(ContactData.class);
            List<ContactData> contacts = (List<ContactData>) xstream.fromXML(xml);

            return contacts.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
        }
    }


    @Test(dataProvider = "validContactsFromXml")
    public void testContactCreation(ContactData contact) {
        if(app.db().groups().size() == 0) {
            app.goTo().groupPage();
            app.group().create(new GroupData().withName(app.getProperty("g.name")));
        }
        Groups groups = app.db().groups();
        ContactData newContact = new ContactData().withFirstName(contact.getFirstName()).withLastName(contact.getLastName())
                .withAddress(contact.getAddress()).withEmail(contact.getEmail()).withHomePhone(contact.getHomePhone())
                .withNickName(contact.getNickName()).withPhoto(contact.getPhoto()).inGroup(groups.iterator().next());
        app.goTo().homePage();
        Contacts before = app.db().contacts();
        app.contact().create(newContact, true);

        assertThat(app.contact().count(), equalTo(before.size() + 1));
        Contacts after = app.db().contacts();

        assertThat(after, equalTo(before.withAdded(contact.withId(after.stream().mapToInt((c) -> c.getId()).max().getAsInt()))));

        verifyContactListInUI();
    }

    @Test(enabled = false)
    public void testCurrentDir() throws IOException {
        File currentDir = new File(".");
        System.out.println(currentDir.getAbsolutePath());
        File photo = new File(app.getProperty("path.contacts.photo"));
        System.out.println(photo.getAbsolutePath());
        System.out.println(photo.exists());
    }


}
