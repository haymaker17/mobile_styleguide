//
//  EvaTime.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EvaTime.h"

@implementation EvaTime

NSDictionary *datetime = nil;

#pragma - mark designated init method

-(id)initWithDict:(NSDictionary *)dictionary
{
    self = [super init];
    if(self)
    {
        datetime = [[NSDictionary alloc] initWithDictionary:dictionary];
        [self parseJson];
    }
    return self;

}

-(void)parseJson
{
    if([datetime objectForKey:@"Date"])
    {
        self.date = [datetime objectForKey:@"Date"];
    }
    
    if([datetime objectForKey:@"Time"])
    {
        self.time = [datetime objectForKey:@"Time"];
    }
    if([datetime objectForKey:@"Delta"])
    {
        self.delta = [datetime objectForKey:@"Delta"];
        self.delta = [self.delta substringFromIndex:[self.delta rangeOfString:@"days=+" ].location];
    }
    if([datetime objectForKey:@"Restriction"])
    {
        self.restriction = [datetime objectForKey:@"Restriction"];
    }
    if([datetime objectForKey:@"Calculated"])
    {
        self.calculated = [datetime objectForKey:@"Calculated"];
    }
}
// Add utility methods to return date/time etc.
-(NSDate *)getDateTime
{
    if(![self.date lengthIgnoreWhitespace] && ![self.time lengthIgnoreWhitespace])
        return nil;
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
    NSString *datestring = nil;
    NSDate *datetime = nil ;
        
    // if time is not given then just give 
    if(![self.time lengthIgnoreWhitespace])
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        datestring = [NSString stringWithFormat:@"%@",self.date];
    }
    else
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
        datestring = [NSString stringWithFormat:@"%@T%@",self.date,self.time];
    }
    datetime = [dateFormatter dateFromString:datestring];
    
    return datetime;
    
}



@end
