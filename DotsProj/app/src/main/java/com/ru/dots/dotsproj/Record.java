package com.ru.dots.dotsproj;

import java.io.Serializable;

/**
 * Created by eddadr on 14.9.2015.
 */
public class Record implements Serializable {

    private String m_date;
    private int m_score;
    private String m_number;

    Record(String date, int score, String num)
    {
        m_date = date;
        m_score = score;
        m_number = num;
    }

    String getDate() { return m_date; }
    int getScore() { return m_score;}
    String getNumber() { return m_number; }
    void setNumber(String nr) { m_number = nr; }

    @Override
    public String toString()
    {
        return m_score + " " + m_date;
    }
}
