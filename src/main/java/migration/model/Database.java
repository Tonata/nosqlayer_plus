package migration.model;

import java.util.ArrayList;
import java.util.Collection;

public class Database {

    private String name;
    private String host;
    private int port;
    private Collection<Table> tables = new ArrayList();

    public Database() {

    }

    public Database(String name,
                    String host,
                    int port) {

        this.name = name;
        this.host = host;
        this.port = port;

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Table> getTables() {
        return tables;
    }

    public void addTable(Table tables) {
        this.tables.add(tables);
    }    
}
