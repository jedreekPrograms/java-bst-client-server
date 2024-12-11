package pl.bst.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.bst.ui.TreeClientView;
public final class TreeClientApplication extends Application {
    private TreeClientView view;
    @Override public void start(Stage stage){view=new TreeClientView();stage.setScene(new Scene(view,850,650));stage.setTitle("BST Client — Java TCP");stage.show();}
    @Override public void stop(){if(view!=null)view.close();}
    public static void main(String[] args){launch(args);}
}
