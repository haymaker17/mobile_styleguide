//
//  MsgResponderJSON.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MsgResponderJSON.h"

@implementation MsgResponderJSON

-(void) flushData
{
}

-(id)init
{
    self = [super init];
	if (self)
    {
        [self flushData];
    }
	return self;
}

-(void) respondToXMLData:(NSData *)data
{
	[self flushData];
	[self parseXMLFileAtData:data];
}

- (void)parseXMLFileAtData:(NSData *)webData
{
    NSLog(@"MsgResponderJSON does not provide an implementation");
}
    
- (void) processJSONParseError:(NSError*) error
{
}

- (void) processJSONParseResult:(id) result
{    
    /*
     Sample Code:
     
     if (![result isKindOfClass:[NSArray class]]) {
     //id name = [result objectForKey:@"name"];
     //if ( name!= nil) {
     self.firstName = [result objectForKey:@"given_name"];
     if (self.firstName == nil)
     self.firstName = @"";
     self.lastName = [result objectForKey:@"family_name"];
     //}
     self.externalId = (NSString*) [result objectForKey:@"id"];
     }

     */
}

- (void)encodeWithCoder:(NSCoder *)coder
{
	[super encodeWithCoder:coder];
}

- (id)initWithCoder:(NSCoder *)coder
{
	return [super initWithCoder:coder];
}


@end
