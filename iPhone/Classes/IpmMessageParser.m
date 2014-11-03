//
//  IpmMessageParser.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "IpmMessageParser.h"
#import "IpmMessage.h"
#import "RXMLElement.h"

@implementation IpmMessageParser

- (id)initWithXmlResponse:(NSString *)response
{
    self = [super init];
    if (self) {
        if (response) {
            _messages = [self parseListOfIpmMessages:response];
        }
    }
    return self;
}

- (NSArray *)parseListOfIpmMessages:(NSString *)xmlString
{
    NSMutableArray *tempMsgs = [[NSMutableArray alloc] init];
    
    // Read the XML from the passed string
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:xmlString encoding:NSUTF8StringEncoding];
    
    // Read the XML array of IPM messages
    NSArray *msgArray = [rootXML children:@"IpmMsg"];
    if (msgArray != nil && [msgArray count] > 0)
    {
        for (RXMLElement *singleMsg in msgArray) {
            // We only want DFP IPM messages, so check they are DFP before adding to the array
            NSString *externalSrcType = [singleMsg child:@"ExternalSrcType"].text;
            if (externalSrcType != nil && [externalSrcType isEqual: @"DFP"])
            {
                IpmMessage *newMessage = [[IpmMessage alloc] init];
                newMessage.target = [singleMsg child:@"Target"].text;
                newMessage.msgKey = [singleMsg child:@"IpmMsgKey"].text;
                newMessage.adUnitId = [singleMsg child:@"ExternalSrc"].text;
                
                // Initialize the dictionary that will hold the additional parameters
                newMessage.additionalParameters = [[NSMutableDictionary alloc]init];
                
                // Add the current app version to the dictionary
                NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
                [newMessage.additionalParameters setValue:ver forKey:@"AppVersion"];

                // Additional parameters are passed as a JSON string
                NSString *customTargeting = [singleMsg child:@"ExternalSrcParams"].text;
                if (customTargeting != nil && [customTargeting length] > 0)
                {
                    // The JSON string contains a key/value pairs, some values are strings, others are arrays
                    NSData *jsonData = [customTargeting dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:nil];
                    
                    if (dict != nil && [dict count] > 0) {
                        [dict enumerateKeysAndObjectsUsingBlock:^(id key, id object, BOOL *stop) {
                            if ([object isKindOfClass:[NSString class]]) {
                                // string values are just copied over
                                [newMessage.additionalParameters setValue:((NSString*)object) forKey:key];
                            }
                            else if ([object isKindOfClass:[NSArray class]]) {
                                // array values need to be converted into strings
                                NSString* joinedArray = [((NSArray*)object) componentsJoinedByString:@","];
                                [newMessage.additionalParameters setValue:joinedArray forKey:key];
                            }
                            else {
                                // Anything else, leave it empty
                                [newMessage.additionalParameters setValue:@"" forKey:key];
                            }
                        }];
                    }

                }
                [tempMsgs addObject:newMessage];
            }
        }
    }
    return tempMsgs;
}

@end
