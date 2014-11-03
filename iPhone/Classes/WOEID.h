//
//  WOEID.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/30/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WOEID : NSObject <NSXMLParserDelegate>{
	NSXMLParser * dataParser;
	NSString * currentElement;
	NSMutableString *woeid;
	
}

//http://query.yahooapis.com/v1/public/yql?q=select%20place.woeid%20from%20flickr.places%20where%20lat%3D45.79%20and%20lon%3D6.97&diagnostics=true
//the above shows how to get a woeid by lat long... call the above url to do this

@property (strong, nonatomic) NSMutableString *woeid;

- (void)parseXMLFileAtURL:(NSString *)URL;
- (void)parseXMLFileAtData:(NSData *)webData;

@end
