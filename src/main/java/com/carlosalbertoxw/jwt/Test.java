/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlosalbertoxw.jwt;

/**
 *
 * @author Carlos
 */
public class Test {

    public static void main(String[] args) {
        JWT jwt = new JWT();
        String token = jwt.createJWT(5);
        System.out.println(token);
        Integer idUser = jwt.verifyJWT(token);
        System.out.println(idUser);
    }
}
