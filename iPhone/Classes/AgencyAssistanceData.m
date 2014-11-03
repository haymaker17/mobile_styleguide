//
//  AgencyAssistanceData.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 14/06/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AgencyAssistanceData.h"
#import "RXMLElement.h"

@implementation AgencyAssistanceData

-(NSString *)getMsgIdKey
{
	return TRAVEL_AGENCY_ASSISTANCE_INFO;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/Agency/GetAgencyAssistance",[ExSystem sharedInstance].entitySettings.uri];
    NSString *itinLocator = parameterBag[@"ITIN_LOCATOR"];
    if (itinLocator) 
        path = [path stringByAppendingFormat:@"?itinLocator=%@",itinLocator];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

//<MWSResponse>
//    <Response>
//        <Agency>
//            <Address><a:string>18400 NE Union Hill Rd</a:string><a:string>Redmond, WA 98052</a:string></Address>
//            <DaytimeHoursEnds>18:00</DaytimeHoursEnds>
//            <DaytimeHoursStarts>8:00</DaytimeHoursStarts>
//            <DaytimeMessage>'Were up in your internets, serving all your travel needs.'</DaytimeMessage>
//            <DaytimePhone>1-800-555-1212</DaytimePhone>
//            <Name>Amex Dev Travel Apollo</Name>
//            <NightHoursEnds>8:00</NightHoursEnds>
//            <NightHoursStarts>18:00</NightHoursStarts>
//            <NightMessage>I just met you.  And this is crazy.  Call me maybe?</NightMessage>
//            <NightPhone>1-800-555-1313</NightPhone>
//            <PreferredPhone>Daytime</PreferredPhone>
//        </Agency>
//    </Response>
//    <Status>
//        <IsSuccess>true</IsSuccess>
//    </Status>
//</MWSResponse>

-(void) respondToXMLData:(NSData *)data
{
    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
    //    theXML = [self getFakeData];
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:theXML encoding:NSUTF8StringEncoding];
    
    NSArray *list = [rootXML childrenWithRootXPath:@"/MWSResponse/Errors/Error/UserMessage"];
    if ([list count])
        self.errorMessage = [(RXMLElement *)[list lastObject] text];
    list = [rootXML childrenWithRootXPath:@"/MWSResponse/Response/Agency"];
    for(RXMLElement *agency in list)
    {
        self.dayPhoneNumber = [[agency child:@"DaytimePhone"] text];
        self.nightPhoneNumber = [[agency child:@"NightPhone"] text];
        self.preferredCallingOption = [[agency child:@"PreferredPhone"] text];
        self.preferredPhoneNumber = [[agency child:[NSString stringWithFormat:@"%@Phone",self.preferredCallingOption]] text];
    }
    list = [rootXML childrenWithRootXPath:@"/MWSResponse/Response/TripRecordLocator"];
    if ([list count]) 
        self.recordLocator = [(RXMLElement *)[list lastObject] text];
    list = [rootXML childrenWithRootXPath:@"/MWSResponse/Response/ItinLocator"];
    if ([list count])
        self.changedItinLocator = [(RXMLElement *)[list lastObject] text];
}


@end
