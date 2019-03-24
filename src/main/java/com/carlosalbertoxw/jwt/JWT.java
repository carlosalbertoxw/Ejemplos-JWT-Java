/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlosalbertoxw.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Carlos
 */
public class JWT {

    private static final String KEY = "qwerty";

    public String createJWT(Integer IdUser) {
        try {
            Date date = new Date();
            date.setTime(date.getTime() + 1000 * 60 * 1);
            String compactJws = Jwts.builder()
                    .setExpiration(date)
                    .claim("id", IdUser)
                    .signWith(SignatureAlgorithm.HS512, KEY.getBytes("UTF-8"))
                    .compact();
            return compactJws;
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public Integer verifyJWT(String compactJws) {
        try {
            Claims claims = Jwts.parser().setSigningKey(KEY.getBytes("UTF-8")).parseClaimsJws(compactJws).getBody();
            if (claims.get("id") != null && !claims.get("id").equals("")) {
                return (Integer) claims.get("id");
            }
        } catch (ExpiredJwtException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ExpiredJwtException:" + e.getMessage(), "");
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
}
