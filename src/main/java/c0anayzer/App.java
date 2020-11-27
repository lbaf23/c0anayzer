package c0anayzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import c0anayzer.analyser.Analyser;
import c0anayzer.error.CompileError;
import c0anayzer.midcode.MidCode;
import c0anayzer.midcode.WriteFile;
import c0anayzer.tokenizer.StringIter;
import c0anayzer.tokenizer.Tokenizer;

//import C0Anayzer.vm.MiniVm;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args) throws CompileError {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("asm");

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        PrintStream output;
        if (outputFileName.equals("-")) {
            output = System.out;
        } else {
            try {
                output = new PrintStream(new FileOutputStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        // analyze
        var analyzer = new Analyser(tokenizer);


        MidCode m = analyzer.analyse();
        output.println(MidCode.getMidCode().toString());
        //WriteFile.writeO0File(m, outputFileName);

        /*
        try {

        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            //e.printStackTrace();
            System.err.println(e);
            System.exit(0);
        }*/
        // output.println(MidCode.getMidCode().toString());
    }

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("c0analyzer");
        var parser = builder.build();
        //parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        //parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("asm")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }

    private static Tokenizer tokenize(StringIter iter) {
        return new Tokenizer(iter);
    }
}
