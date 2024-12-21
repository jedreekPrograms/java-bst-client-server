package pl.bst.examples;

import java.io.*;
/** Preserves the original standalone byte-stream exercise, separate from the server. */
public final class ByteStreamDemo {
    private ByteStreamDemo(){ }
    public static void main(String[] args)throws IOException{
        System.out.println("Enter up to 10 bytes:");
        ByteArrayOutputStream buffer=new ByteArrayOutputStream(10);
        while(buffer.size()<10){int next=System.in.read();if(next==-1)break;buffer.write(next);}
        byte[] bytes=buffer.toByteArray();
        System.out.println("Read bytes: "+bytes.length);
        try(var input=new ByteArrayInputStream(bytes)){
            int next;while((next=input.read())!=-1)System.out.print(Character.toUpperCase((char)next)+" ");
        }
        System.out.println();
    }
}
