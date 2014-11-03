//
//  ExTestAuthenticateData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExTestAuthenticateData.h"
#import "Authenticate.h"
#import "ExUnitTestDescriber.h"

@implementation ExTestAuthenticateData

-(id)init
{
    self = [super init];
    if (self)
    {
        aFails = [[NSMutableArray alloc] initWithObjects:nil];
    }
    return self;
}




-(NSString *) dumpFails
{
    NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
    
    int failCount = 0;
    
    for(NSString *result in aFails)
    {
        failCount++;
        if([s length] > 0)
            [s appendString:@"; "];
        
        [s appendString:result];
    }
    
    NSString *rVal = [NSString stringWithFormat:@"Failure: %@", s];
    [aFails removeAllObjects];
    return rVal;
}


-(NSMutableArray *)runTests
{
    NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
    
    ExUnitTestDescriber *describer = [[ExUnitTestDescriber alloc] initWithValues:@"runLoginResultUnitTest" key:@"Authenticate Data"];
    if([self runLoginResultUnitTest])
        describer.isFail = NO;
    else 
    {
        describer.failString = [self dumpFails];
        describer.isFail = YES;
    }
    [a addObject:describer];
    
    describer = [[ExUnitTestDescriber alloc] initWithValues:@"runPositiveUnitTest" key:@"Authenticate Data"];
    if([self runPositiveUnitTest])
        describer.isFail = NO;
    else 
    {
        describer.failString = [self dumpFails];
        describer.isFail = YES;
    }
    [a addObject:describer];

    
    return a;
}


-(BOOL) runLoginResultUnitTest
{
    BOOL success = YES;
    NSString *xmlString = [self fetchLoginResult];
    Authenticate *responder = [[Authenticate alloc] init];
    [responder parseXMLFileAtData:[xmlString dataUsingEncoding:NSStringEncodingConversionAllowLossy]];
    
    
//    if(![responder.userName isEqualToString:@"jksws@snwnov.com"])
//    {
//        [aFails addObject:[NSString stringWithFormat:@"responder.userName = %@", responder.userName]];
//
//        return NO;
//    }
    
    if(![responder.entityType isEqualToString:@"Corporate"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.entityType = %@", responder.entityType]];
        return NO;
    }
    
    if(![responder.expenseCtryCode isEqualToString:@"US"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.expenseCtryCode = %@", responder.expenseCtryCode]];
        return NO;
    }
    
    if(![responder.crnCode isEqualToString:@"USD"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.crnCode = %@", responder.crnCode]];
        return NO;
    }
    
    if(![responder.roles isEqualToString:@"CESUser,TravelUser,MOBILE_EXPENSE_TRAVELER,Dining_User,Taxi_User, Amtrak_User"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.roles = %@", responder.roles]];
        return NO;
    }
    
    if(![responder.sessionID isEqualToString:@"4CBDB4B3-36D5-4F90-813A-6FFECAFBE0A3"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.sessionID = %@", responder.sessionID]];
        return NO;
    }
    
    if(![responder.timedOut isEqualToString:@"120"])
    {
        [aFails addObject:[NSString stringWithFormat:@"responder.timedOut = %@", responder.timedOut]];
        return NO;
    }
    
    
    
    return success;
}


-(BOOL) runPositiveUnitTest
{
    [aFails addObject:@"Not feeling so positive anymore.  Something just kind of broke inside and then where once there were unicorns and rainbows, there are now just a bunch of clowns that shriek angrily."];
    return NO;
}


#pragma mark -
#pragma mark Get Data methods
-(NSString *) fetchLoginResult
{
    NSString *xmlData = @"<LoginResult xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><EntityType>Corporate</EntityType><ExpenseCtryCode>US</ExpenseCtryCode><PinExpirationDate>2011-05-25T13:45:00</PinExpirationDate><RemoteWipe>N</RemoteWipe><RolesMobile>CESUser,TravelUser,MOBILE_EXPENSE_TRAVELER,Dining_User,Taxi_User, Amtrak_User</RolesMobile><ServiceURIs><ServiceInfo><Name>Expense</Name><URI>mobile/Expense</URI></ServiceInfo></ServiceURIs><Session><ID>4CBDB4B3-36D5-4F90-813A-6FFECAFBE0A3</ID><TimeOut>120</TimeOut></Session><SiteSettings><SiteSetting><Name>IS_DATE_EDITABLE</Name><Type>CARD</Type><Value>Y</Value></SiteSetting></SiteSettings><TwitterUrl>http://api.twitter.com/1/statuses/user_timeline.xml?screen_name=Concur2Go</TwitterUrl><UserContact><CompanyName>snwnov</CompanyName><Email>jksws@snwnov.com</Email><FirstName>William</FirstName><LastName>Never</LastName></UserContact><UserCrnCode>USD</UserCrnCode><UserId>nq4escHeWmhk3lDNd51xuFHwlPGHPxYpx</UserId></LoginResult>";
    return xmlData;
}

@end
