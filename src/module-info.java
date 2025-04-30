module DigitalSignature {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;
	requires jdk.httpserver;
	requires jdk.crypto.cryptoki;

	
	requires org.apache.pdfbox;
	requires org.apache.fontbox;
	requires commons.logging;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.provider;
	requires org.bouncycastle.util;
	requires org.bouncycastle.mail;
	requires java.naming;
	requires com.sun.jna;
	requires commons.fileupload;
	requires org.json;
	
	//requires ALL-DEFAULT;

	opens application.controllers to javafx.graphics, javafx.fxml;
	exports application.controllers;
	exports application.exceptions;
	exports application.server;
	exports application.services;
	exports application.ui;
	exports application.utils;
	
	exports application;
}
