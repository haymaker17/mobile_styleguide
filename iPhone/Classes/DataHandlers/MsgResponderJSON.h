//
//  MsgResponderJSON.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"

// Handle response in JSON
@interface MsgResponderJSON : MsgResponder
{
    
}


//- (void)parseXMLFileAtData:(NSData *)webData;
// Subclass overrides these to process data
- (void) processJSONParseError:(NSError*) error;
- (void) processJSONParseResult:(id) result;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

-(void) flushData;
-(id)init;

-(void) respondToXMLData:(NSData *)data;

@end
