//
//  ReportEntryDetailData.m
//  ConcurMobile
//
//  Created by yiwen on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportEntryDetailData.h"
#import "DataConstants.h"
#import "DateTimeFormatter.h"

@implementation ReportEntryDetailData
@synthesize rpeKey, carDetail, carRate, carRateType;

-(NSString *)getMsgIdKey
{
	return REPORT_ENTRY_DETAIL_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.roleCode = parameterBag[@"ROLE_CODE"];
	self.rpeKey = parameterBag[@"RPE_KEY"];
	self.rptKey = parameterBag[@"RPT_KEY"];
	
	if (![roleCode length])
		self.roleCode = ROLE_EXPENSE_TRAVELER;
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportEntryDetailV4/%@/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.rpeKey, self.roleCode];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	if ([elementName isEqualToString:@"ReportEntryDetail"])
	{
		rpt = [[ReportData alloc] init];
		inReport = YES;
		inComment = NO;
		inItemize = NO;
		inCompanyDisbursements = NO;
		inFormField = NO;
		inAttendee = NO;
		inEntry = YES;
	}
    else if ([elementName isEqualToString:@"CarConfig"])
	{
		self.rpt.entry.carConfig = [[CarConfigData alloc] init];
	}
	else if ([elementName isEqualToString:@"CarRate"])
	{
        //		NSLog(@"car rate alloc");
		self.carRate = [[CarRateData alloc] init];
	}
	else if ([elementName isEqualToString:@"CarDetail"])
	{
        //		NSLog(@"car detail alloc");
		self.carDetail = [[CarDetailData alloc] init];
	}
	else if ([elementName isEqualToString:@"CarRateType"])
	{
        //		NSLog(@"car rate type alloc");
		self.carRateType = [[CarRateTypeData alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"ReportEntryDetail"])
	{
        if ([self.rpt.entry.ereceiptId lengthIgnoreWhitespace] && ![self.rpt.entry.eReceiptImageId lengthIgnoreWhitespace])
            self.rpt.entry.eReceiptImageId = @"HACK e-receipt Image ID";
        
        //MOB-21147 - had to do it three times otherwise status is lost due to keyArchiver
        if (self.rpt.entry.eReceiptImageId != nil) {
            // MOB-21147 - Set hasReceipt
            // if its e-receipt then copy ereceipt image id to receiptimage id field. Also mark that we have a receipt
            [rpt.entry setHasMobileReceipt:@"Y"];
            [rpt.entry setReceiptImageId:self.rpt.entry.eReceiptImageId];
        }
	}
    else if ([elementName isEqualToString:@"CarConfig"])
	{
	}
	else if ([elementName isEqualToString:@"CarRate"])
	{
		[self.rpt.entry.carConfig.aCarRateKeys addObject:carRate.key];
		(self.rpt.entry.carConfig.dictCarRates)[carRate.key] = carRate;
		if(carRateType != nil)
		{
            //			NSLog(@"carRate rate %@", carRate.rate);
			[carRateType.aCarRateKeys addObject:carRate.key];
			(carRateType.dictCarRates)[carRate.key] = carRate;
		}
	}
	else if ([elementName isEqualToString:@"CarDetail"])
	{
        //		NSLog(@"carDetail.carKey %@", carDetail.carKey);
		[self.rpt.entry.carConfig.aCarDetailKeys addObject:carDetail.carKey];
		(self.rpt.entry.carConfig.dictCarDetails)[carDetail.carKey] = carDetail;
	}
	else if ([elementName isEqualToString:@"CarRateType"])
	{
        //		NSLog(@"CarRateType rate (adding to detail) count %d", [carRateType.aCarRateKeys count]);
		[carDetail.aCarRateTypes addObject:carRateType];
	}
	else
		[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"CanCreateExp"])
	{
		[self.rpt.entry.carConfig setCanCreateExp:buildString];
	}
	else if ([currentElement isEqualToString:@"CarcfgKey"])
	{
		[self.rpt.entry.carConfig setCarcfgKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CompanyOrPersonal"])
	{
		[self.rpt.entry.carConfig setCompanyOrPersonal:buildString];
		if([buildString isEqualToString:@"PER"])
			self.rpt.entry.carConfig.isPersonal = YES;
	}
	else if ([currentElement isEqualToString:@"ConfigType"])
	{
		[self.rpt.entry.carConfig setConfigType:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[self.rpt.entry.carConfig setCrnCode:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnKey"])
	{
		[self.rpt.entry.carConfig setCrnKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CtryCode"])
	{
		[self.rpt.entry.carConfig setCtryCode:buildString];
	}
	else if ([currentElement isEqualToString:@"CtryDistanceUnitCode"])
	{
		[self.rpt.entry.carConfig setCtryDistanceUnitCode:buildString];
	}
	else if ([currentElement isEqualToString:@"Rate"])
	{
		[carRate setRate:buildString];
        
	}
	else if ([currentElement isEqualToString:@"StartDate"])
	{
		[carRate setStartDate:buildString];
		[carRate setKey:buildString];
		[carRate setDateStart:[DateTimeFormatter getNSDateFromMWSDateString:buildString]];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"]];
	}
	//car detail
	else if ([currentElement isEqualToString:@"CarKey"])
	{
		[carDetail setCarKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CriteriaName"])
	{
		[carDetail setCriteriaName:buildString];
	}
	else if ([currentElement isEqualToString:@"DistanceToDate"])
	{
		[carDetail setDistanceToDate:buildString];
	}
	else if ([currentElement isEqualToString:@"IsPreferred"])
	{
		[carDetail setIsPreferred:buildString];
	}
	else if ([currentElement isEqualToString:@"VehicleId"])
	{
		[carDetail setVehicleId:buildString];
	}
	else if ([currentElement isEqualToString:@"OdometerStart"])
	{
        if ([buildString  lengthIgnoreWhitespace])
            [carDetail setOdometerStart:[buildString integerValue]];
	}
	//car rate type
	else if ([currentElement isEqualToString:@"LowerLimit"])
	{
		[carRateType setLowerLimit:buildString];
		[carRateType setILower:[buildString intValue]];
	}
	else if ([currentElement isEqualToString:@"RateType"])
	{
		[carRateType setRateType:buildString];
		[carRateType setIUpper:[buildString intValue]];
	}
	else if ([currentElement isEqualToString:@"UpperLimit"])
	{
		[carRateType setUpperLimit:buildString];
	}
	
}



#pragma mark -
#pragma mark PartialReportDataBase Methods
-(ReportData*) updateReportObject:(ReportData*) obj
{
	if (obj == nil)
		return obj;
    
	// Update the entry object
	if (rpt.entry.parentRpeKey != nil)
	{
		// Find the parent entry first
		EntryData* parentEntry = (obj.entries)[rpt.entry.parentRpeKey];
		EntryData* itemEntry = (parentEntry.items)[rpt.entry.rpeKey];
		if (itemEntry == nil)
		{
			[parentEntry.itemKeys addObject:rpt.entry.rpeKey];
		}
		(parentEntry.items)[rpt.entry.rpeKey] = rpt.entry;
	}
	else 
	{
		EntryData* oldEntry = (obj.entries)[rpt.entry.rpeKey];
		if (oldEntry == nil)
		{
			[obj.keys insertObject:rpt.entry.rpeKey atIndex:0]; // Latest entry goes to the top
		}
		(obj.entries)[rpt.entry.rpeKey] = rpt.entry;			
	}

	return obj;
}

@end
