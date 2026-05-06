package com.permuta.factory;

import com.permuta.adapter.teacherCode.controller.impl.BibliotecaAdapter;
import com.permuta.adapter.teacherCode.controller.impl.CompraAdapter;
import com.permuta.adapter.teacherCode.controller.impl.JuegoAdapter;
import com.permuta.adapter.teacherCode.controller.impl.ReseniaAdapter;
import com.permuta.adapter.teacherCode.controller.impl.UsuarioAdapter;
import org.ainhoamarfer.controlador.BibliotecaControlador;
import org.ainhoamarfer.controlador.CompraControlador;
import org.ainhoamarfer.controlador.JuegosControlador;
import org.ainhoamarfer.controlador.ResenasControlador;
import org.ainhoamarfer.controlador.UsuarioControlador;
import org.ainhoamarfer.repositorio.implementacion_hibernate.BibliotecaRepoHibernate;
import org.ainhoamarfer.repositorio.implementacion_hibernate.CompraRepoHibernate;
import org.ainhoamarfer.repositorio.implementacion_hibernate.JuegoRepoHibernate;
import org.ainhoamarfer.repositorio.implementacion_hibernate.ResenaRepoHibernate;
import org.ainhoamarfer.repositorio.implementacion_hibernate.UsuarioRepoHibernate;
import org.ainhoamarfer.repositorio.implementacion_memoria.BibliotecaRepo;
import org.ainhoamarfer.repositorio.implementacion_memoria.CompraRepo;
import org.ainhoamarfer.repositorio.implementacion_memoria.JuegoRepo;
import org.ainhoamarfer.repositorio.implementacion_memoria.ResenaRepo;
import org.ainhoamarfer.repositorio.implementacion_memoria.UsuarioRepo;
import org.ainhoamarfer.transaction.HibernateTransactionManager;
import org.ainhoamarfer.transaction.ISesionManager;
import org.ainhoamarfer.transaction.ITransactionManager;
import org.ainhoamarfer.transaction.NoOpTransactionManager;

import java.lang.reflect.Field;
import java.util.List;

public final class AdapterFactory {

    private static final boolean IN_MEMORY = false;

    public static AdapterBundle getAdapterBundle(String groupID)
            throws NoSuchFieldException, IllegalAccessException {
        if (IN_MEMORY) {
            return getInMemoryBundle();
        } else {
            return getHibernateBundle();
        }

    }

    private static AdapterBundle getHibernateBundle() {
        ITransactionManager tm = new HibernateTransactionManager();

        tm.inTransaction(() -> {

           //Borrar tabla de Usuarios
            ((ISesionManager)tm).getSession().createMutationQuery("DELETE FROM Usuario").executeUpdate();
            //Borrar tabla de Juegos
            ((ISesionManager)tm).getSession().createMutationQuery("DELETE FROM Juego").executeUpdate();
            //Borrar tabla de Compras
            ((ISesionManager)tm).getSession().createMutationQuery("DELETE FROM Compra").executeUpdate();
            //Borrar tabla de Reseñas
            ((ISesionManager)tm).getSession().createMutationQuery("DELETE FROM Resena").executeUpdate();
            //Borrar tabla de Bibliotecas
            ((ISesionManager)tm).getSession().createMutationQuery("DELETE FROM Biblioteca").executeUpdate(); 
            return null;
        });

        var usuarioRepo = new UsuarioRepoHibernate((ISesionManager) tm);
        var juegoRepo = new JuegoRepoHibernate((ISesionManager) tm);
        var compraRepo = new CompraRepoHibernate((ISesionManager) tm);
        var resenaRepo = new ResenaRepoHibernate((ISesionManager) tm);
        var bibliotecaRepo = new BibliotecaRepoHibernate((ISesionManager) tm);

        UsuarioControlador usuarioCtrl = new UsuarioControlador(usuarioRepo, tm);
        JuegosControlador juegoCtrl = new JuegosControlador(juegoRepo, tm);
        CompraControlador compraCtrl = new CompraControlador(compraRepo, juegoRepo, usuarioRepo, bibliotecaRepo, tm);
        ResenasControlador resenasCtrl = new ResenasControlador(resenaRepo, bibliotecaRepo, juegoRepo, usuarioRepo, tm);
        BibliotecaControlador bibliotecaCtrl = new BibliotecaControlador(bibliotecaRepo, usuarioRepo, juegoRepo, tm);


        return new AdapterBundle(
                new UsuarioAdapter(usuarioCtrl),
                new CompraAdapter(compraCtrl),
                new JuegoAdapter(juegoCtrl),
                new ReseniaAdapter(resenasCtrl),
                new BibliotecaAdapter(bibliotecaCtrl));
    }

    private static AdapterBundle getInMemoryBundle() throws NoSuchFieldException, IllegalAccessException {
        ITransactionManager tm = new NoOpTransactionManager();
        resetRepoStatics(UsuarioRepo.class, "USUARIOS", "idContador");
        resetRepoStatics(JuegoRepo.class, "JUEGOS", "idContador");
        resetRepoStatics(CompraRepo.class, "COMPRAS", "idContador");
        resetRepoStatics(ResenaRepo.class, "RESENAS", "idContador");
        resetRepoStatics(BibliotecaRepo.class, "BIBLIOTECAS", "idContador");

        UsuarioRepo usuarioRepo = new UsuarioRepo();
        JuegoRepo juegoRepo = new JuegoRepo();
        CompraRepo compraRepo = new CompraRepo();
        ResenaRepo resenaRepo = new ResenaRepo();
        BibliotecaRepo bibliotecaRepo = new BibliotecaRepo();

        UsuarioControlador usuarioCtrl = new UsuarioControlador(usuarioRepo, tm);
        JuegosControlador juegoCtrl = new JuegosControlador(juegoRepo, tm);
        CompraControlador compraCtrl = new CompraControlador(compraRepo, juegoRepo, usuarioRepo, bibliotecaRepo, tm);
        ResenasControlador resenasCtrl = new ResenasControlador(resenaRepo, bibliotecaRepo, juegoRepo, usuarioRepo, tm);
        BibliotecaControlador bibliotecaCtrl = new BibliotecaControlador(bibliotecaRepo, usuarioRepo, juegoRepo, tm);

        return new AdapterBundle(
                new UsuarioAdapter(usuarioCtrl),
                new CompraAdapter(compraCtrl),
                new JuegoAdapter(juegoCtrl),
                new ReseniaAdapter(resenasCtrl),
                new BibliotecaAdapter(bibliotecaCtrl));
    }

    @SuppressWarnings("unchecked")
    private static void resetRepoStatics(Class<?> repoClass, String listFieldName, String counterFieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field listField = repoClass.getDeclaredField(listFieldName);
        listField.setAccessible(true);
        ((List<?>) listField.get(null)).clear();

        Field counterField = repoClass.getDeclaredField(counterFieldName);
        counterField.setAccessible(true);
        counterField.set(null, 1L);
    }
}
