//
//  CarRatesData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CarRatesData.h"
#import "DateTimeFormatter.h"
#import "DataConstants.h"
#import "FormatUtils.h"
#import "ExSystem.h"

@implementation CarRatesData

@synthesize items, obj, keys, carRate, carDetail, carRateType;

// not a singleton, but rather this is a static reference to the last carRatesData downloaded
static id lastCarRatesDataDownloaded = nil;

+ (id)lastCarRatesDataDownloaded
{
    return lastCarRatesDataDownloaded;
}

-(NSString *)getMsgIdKey
{
	return CAR_RATES_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
    
	self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetCarConfigs",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

-(void) flushData
{
    [super flushData];
    self.items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];
}


//	<ArrayOfCarConfig xmlns="http://schemas.datacontract.org/2004/07/Snowbird" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
//	<CarConfig>
//	<CanCreateExp>Y</CanCreateExp>
//	<CarRates>
//	<CarRate>
//    <Rate>0.55000000</Rate>
//    <StartDate>2005-01-01T00:00:00</StartDate>
//	</CarRate>
//	<CarRate><Rate>0.50000000</Rate><StartDate>2010-01-01T00:00:00</StartDate></CarRate>
//	</CarRates>
//	<CarcfgKey>1</CarcfgKey><CompanyOrPersonal>PER</CompanyOrPersonal>
//	<ConfigType>PER_ONE</ConfigType><CrnKey>1</CrnKey><CtryCode>US</CtryCode><CtryDistanceUnitCode>MILE</CtryDistanceUnitCode>
//	</CarConfig>
//	</ArrayOfCarConfig>

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{

	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"CarConfig"])
	{		
		self.obj = [[CarConfigData alloc] init];
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
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
	
	if ([elementName isEqualToString:@"CarConfig"])
	{
		if (obj.carcfgKey != nil) 
		{
			items[obj.carcfgKey] = obj;
			[keys addObject:obj.carcfgKey];
		}
	}
	else if ([elementName isEqualToString:@"CarRate"])
	{
		[obj.aCarRateKeys addObject:carRate.key];
		(obj.dictCarRates)[carRate.key] = carRate;
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
		[obj.aCarDetailKeys addObject:carDetail.carKey];
		(obj.dictCarDetails)[carDetail.carKey] = carDetail;
	}
	else if ([elementName isEqualToString:@"CarRateType"])
	{
//		NSLog(@"CarRateType rate (adding to detail) count %d", [carRateType.aCarRateKeys count]);
		[carDetail.aCarRateTypes addObject:carRateType];
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"CanCreateExp"])
	{
		[obj setCanCreateExp:buildString];
	}
	else if ([currentElement isEqualToString:@"CarcfgKey"])
	{
		[obj setCarcfgKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CompanyOrPersonal"])
	{
		[obj setCompanyOrPersonal:buildString];
		if([buildString isEqualToString:@"PER"])
			obj.isPersonal = YES;
	}
	else if ([currentElement isEqualToString:@"ConfigType"])
	{
		[obj setConfigType:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[obj setCrnCode:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnKey"])
	{
		[obj setCrnKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CtryCode"])
	{
		[obj setCtryCode:buildString];
	}
	else if ([currentElement isEqualToString:@"CtryDistanceUnitCode"])
	{
		[obj setCtryDistanceUnitCode:buildString];
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
        if ([buildString lengthIgnoreWhitespace])
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

/**
 Used to save the last carRatesData downloaded.  Part of the hack that fixes MOB-17609.
 */
- (void)parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    lastCarRatesDataDownloaded = self;
}

-(float) fetchRate:(NSDate *)date isPersonal:(BOOL)isPersonal isPersonalPartOfBusiness:(BOOL)isPersonalPartOfBusiness distance:(NSString *)distance
            carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode numPassengers:(NSString *)numPassengers distanceToDate:(int) distanceToDate
{
 	float rate = 0.0;
	float amt = 0.0; //variable is going to return the actual amount...
	int passengerCount = [numPassengers intValue];
    
    for(NSString *key in items)
    {
        CarConfigData *carConfig =  items[key];
        
        if(carConfig.isPersonal==isPersonal && [carConfig.aCarDetailKeys count] == 0)
        {
            CarRateData *carRateToUse = [carConfig findRateForDate:date];
            if(carRateToUse != nil)
            {
                return [carRateToUse.rate floatValue];
            }
        }
        else if(carConfig.isPersonal==isPersonal && [carConfig.aCarDetailKeys count] > 0)// && [carConfig.ctryCode isEqualToString:ctryCode]) // && [carConfig.ctryCode isEqualToString:crncyCode]) //still awaiting the crnCode in the rate feed...
        {//we are variable
            
            CarDetailData *cd = [carConfig findDetailForPreferredOrKey: carKey];
            
            if(carConfig.isPersonal==isPersonal && cd != nil)//  && [carConfig.ctryCode isEqualToString:ctryCode])
            {
                int iDistance = [distance intValue];
                for(CarRateTypeData *crtd in cd.aCarRateTypes)
                {
                    
                    int distanceForRate = [crtd distanceInRateFrom:distanceToDate to:distanceToDate + iDistance];                        
                    
                    if(distanceForRate > 0 && !isPersonalPartOfBusiness && [crtd.aCarRateKeys count] > 0 && ([crtd.rateType isEqualToString:@"PER_VAR_CAR"] || [crtd.rateType isEqualToString:@"COM_FIXED_BUS"])) //iDistance >= lower && iDistance <= upper
                    {
                        CarRateData *carRateToUse = [crtd rateForDate:date];							
                        if(carRateToUse != nil)
                        {
                            //NSLog(@"cd key=%@ criteria=%@", cd.carKey, cd.criteriaName);
                            amt = amt + ([carRateToUse.rate floatValue] * distanceForRate);
                        }
                    }
                    else if(iDistance > 0  && !isPersonalPartOfBusiness && [crtd.aCarRateKeys count] > 0 && [crtd.rateType isEqualToString:@"PER_VAR_PAS"] && passengerCount > 0)
                    {
                        CarRateData *carRateToUse = [crtd rateForDate:date];
                        if(carRateToUse != nil)
                        {
                            amt = amt + (([carRateToUse.rate floatValue] * iDistance) * passengerCount);
                        }
                        
                    }
                    else if (iDistance > 0 && isPersonalPartOfBusiness && [crtd.aCarRateKeys count] > 0 && [crtd.rateType isEqualToString:@"COM_FIXED_PER"])
                    {
                        CarRateData *carRateToUse = [crtd rateForDate:date];							
                        if(carRateToUse != nil)
                        {
                            amt = amt + ([carRateToUse.rate floatValue] * distanceForRate);
                        } 
                    }
                    // MAN-23252
                    else if(iDistance > 0  && !isPersonalPartOfBusiness && [crtd.aCarRateKeys count] > 0 && [crtd.rateType isEqualToString:@"COM_FIXED_PAS"] && passengerCount > 0)
                    {
                        CarRateData *carRateToUse = [crtd rateForDate:date];
                        if(carRateToUse != nil)
                        {
                            amt = amt + (([carRateToUse.rate floatValue] * iDistance) * passengerCount);
                        }
                        
                    }
                    
                }
                return amt;
            }
        }
    }
	
    return rate;
    
}




-(CarRateData *) fetchCarRate:(NSDate *)date isPersonal:(BOOL)isPersonal distance:(NSString *)distance carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode
{
	//float rate = 0.0;
	if(isPersonal)
	{
		for(NSString *key in items)
		{
			CarConfigData *carConfig =  items[key];
			if(carConfig.isPersonal && [carConfig.aCarDetailKeys count] == 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
			{
				CarRateData *carRateToUse = nil;
				for(NSString *carRateKey in carConfig.aCarRateKeys)
				{
					CarRateData *cRate = (carConfig.dictCarRates)[carRateKey];
					//NSLog(@"cRate.dateStart = %@", cRate.dateStart);
					if ([date compare:cRate.dateStart] == NSOrderedDescending)
					{
						//NSOrderedDescending: //date is greater than carConfig start date
						if(carRateToUse == nil)
							carRateToUse = cRate;
						else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
							carRateToUse = cRate; //this looped car rate is closer to the date we want...
						
					}
				}
				
				if(carRateToUse != nil)
					return carRateToUse;
			}
			else if(carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0)// && [carConfig.ctryCode isEqualToString:ctryCode])
			{
                
				CarDetailData *cd = (carConfig.dictCarDetails)[carKey];
				
				if(cd == nil)
				{//try to find the preferred
					for(NSString *key in carConfig.dictCarDetails)
					{
						CarDetailData *cdFound = (carConfig.dictCarDetails)[key];
						if([cdFound.isPreferred isEqualToString:@"Y"])
						{
							cd = cdFound;
							break;
						}
					}
				}
				
				if(carConfig.isPersonal && cd != nil)
				{//we have found the vehicle to get rates from...
					CarRateData *carRateToUse = nil;
					int distanceToDate = [cd.distanceToDate intValue];
					
					for(CarRateTypeData *crtd in cd.aCarRateTypes)
					{
                        //						int lower = [crtd.lowerLimit intValue];
                        //						int upper = [crtd.upperLimit intValue];
                        //						int iDistance = [distance intValue];
                        //						
                        //						if(iDistance >= lower && iDistance <= upper)
						int lower = [crtd.lowerLimit intValue];
						int upper = [crtd.upperLimit intValue];
						int iDistance = [distance intValue];
						if(crtd.lowerLimit == nil)
							lower = -1;
						
						if(crtd.upperLimit == nil)
							upper = -1;
						
						int distanceForRate = 0;
						//int distanceAdjuster = 1;
                        //						if(lower == 0)
                        //							distanceAdjuster = 0;
						
						if((iDistance + distanceToDate) >= lower && (iDistance + distanceToDate) <= upper)
							distanceForRate = iDistance; // + distanceToDate; // ((iDistance + distanceAdjuster) - lower);
                        
						if(distanceForRate > 0 && [crtd.aCarRateKeys count] > 0) //iDistance >= lower && iDistance <= upper
						{
                            
							for(NSString *carRateKey in crtd.dictCarRates)
							{
								CarRateData *cRate = (crtd.dictCarRates)[carRateKey];
								//NSLog(@"cRate.dateStart = %@", cRate.dateStart);
								if ([date compare:cRate.dateStart] == NSOrderedDescending)
								{
									//NSOrderedDescending: //date is greater than carConfig start date
									if(carRateToUse == nil)
										carRateToUse = cRate;
									else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
										carRateToUse = cRate; //this looped car rate is closer to the date we want...
									
								}
							}
						}
					}
					
					if(carRateToUse != nil)
						return carRateToUse;
				}
			}
		}
	}
	
	return nil;
}


-(NSString *) fetchDistanceUnit:(BOOL)isPersonal ctryCode:(NSString *)ctryCode
{
    
	if(isPersonal)
	{
		for(NSString *key in items)
		{
			CarConfigData *carConfig =  items[key];
			if(carConfig.isPersonal && [carConfig.configType isEqualToString:@"PER_1"])// && [carConfig.ctryCode isEqualToString:ctryCode])
			{
				NSString *du = [NSString stringWithFormat:@"%@ %@", carConfig.crnKey,  carConfig.ctryDistanceUnitCode];
				return du;
			}
			else if(carConfig.isPersonal && [carConfig.configType isEqualToString:@"PER_VARIABLE"])// && [carConfig.ctryCode isEqualToString:ctryCode])
			{//personal variable rate
				NSString *du = [NSString stringWithFormat:@"%@ %@", carConfig.crnKey,  carConfig.ctryDistanceUnitCode];
				return du;
			}
		}
	}
	
	return @"MILE";
}


-(NSString *) fetchDistanceUnitAndRate:(BOOL)isPersonal date:(NSDate *)date ctryCode:(NSString *)ctryCode distance:(NSString *)distance carKey:(NSString *)carKey
{
	if(isPersonal)
	{
		for(NSString *key in items)
		{
			CarConfigData *carConfig =  items[key];
			if(carConfig.isPersonal)// && [carConfig.ctryCode isEqualToString:ctryCode])
			{
				CarRateData *crd = [self fetchCarRate:date isPersonal:YES distance:distance carKey:carKey ctryCode:ctryCode];
				NSString *du = @"";
				if(crd != nil)
					du = [NSString stringWithFormat:@"%@ %@", [FormatUtils formatMoney:crd.rate crnCode:carConfig.crnCode],  carConfig.ctryDistanceUnitCode];
				else 
					du = carConfig.ctryDistanceUnitCode;
				
				return du;
			}
		}
	}
	
	return @"MILE";
}


-(BOOL) isPersonalVariable:(NSString *) ctryCode
{
	BOOL isPV = NO;
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		if(carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0)// && [carConfig.ctryCode isEqualToString:ctryCode])
			return YES;
	}
	
	return isPV;
}

// DMB

/*!
 fetchCarConfig
 scroll round carConfigData until we find a carKey match
 NOTE Personal Car PER_ONE rate, this has a carKey of -1
 */
-(CarConfigData *) fetchCarConfig: (NSString *) carKey
{
    for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
        // Check rates availability under PER_ONE (fixed) or cars availability under PER_VARIABLE
		if([carKey isEqualToString:@"-1"] &&
           [carConfig.configType isEqualToString:@"PER_ONE"] && [carConfig.dictCarRates count] > 0) {
            
            return carConfig;
        } else {
            if ([carConfig.aCarDetailKeys count] > 0) {
                for(NSString *key in carConfig.dictCarDetails)
                {
                    if ([carKey isEqualToString:key]) {
                        return carConfig;
                    }
                }
            }
        }
        
    }
    return nil;
    
}
/*!
 fetchCarDetail
 scroll round carConfigData until we find a carKey match
 NOTE Personal Car PER_ONE rate, this has a carKey of -1
 */
-(CarDetailData *) fetchCarDetail: (NSString *) carKey
{
    for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
        // Check rates availability under PER_ONE (fixed) or cars availability under PER_VARIABLE
		if([carKey isEqualToString:@"-1"] &&
           [carConfig.configType isEqualToString:@"PER_ONE"] && [carConfig.dictCarRates count] > 0) {
            CarDetailData * cd = [[CarDetailData alloc] init];
            [cd setCarKey:@"-1"];
            [cd setIsPreferred:@"Y"];
            [cd setVehicleId:[Localizer getLocalizedText:@"Personal Car"]];
            return cd;
        } else {
            if ([carConfig.aCarDetailKeys count] > 0) {
                for(NSString *key in carConfig.dictCarDetails)
                {
                    if ([carKey isEqualToString:key]) {
                        CarDetailData *cd = (carConfig.dictCarDetails)[key];
                        return cd;
                    }
                }
            }
        }
        
    }
    return nil;
    
}
/*!
 fetchCarDetailDefault
 uses fetchCarDetailsOrdered, return the default car detail based on the following criteria.
 only 1 car.
 only 1 is preferred car details record
 Personal car will act as if the is preferred is set to Y
 */
-(CarDetailData *) fetchCarDetailDefault
{
    CarDetailData * cd = nil;
    
    NSArray * carDetails = [self fetchCarDetailsOrdered];
    // only 1 record
    if ([carDetails count] > 0)
    {
        if ([carDetails count] == 1) {
            cd = carDetails[0];
        } else {
            CarDetailData * cd1 = carDetails[0];
            CarDetailData * cd2 = carDetails[1];
            if ([[cd1 isPreferred] isEqualToString:@"Y"] && [[cd2 isPreferred] isEqualToString:@"N"]) {
                cd = cd1;
            }
        }
    }
    return cd;
    
}
/*!
 fetchCarDetailsOrdered
 uses fetchCarDetails and orders by is preferred / vehicle id
 */
-(NSArray *) fetchCarDetailsOrdered
{
    NSMutableDictionary *dict = [self fetchCarDetails];
    
    NSSortDescriptor *preferred = [[NSSortDescriptor alloc] initWithKey:@"isPreferred" ascending:NO];
    NSSortDescriptor *vehicleId = [[NSSortDescriptor alloc] initWithKey:@"vehicleId" ascending:YES];
    
    NSArray *sortDescriptors = @[preferred, vehicleId];
    NSArray *sortedArray = [[dict allValues] sortedArrayUsingDescriptors:sortDescriptors];
    
    return sortedArray;
    
}

/*!
 fetchCarDetails
 fetch active personal, company car details & include personal car (PER_ONE rate) if applicable
 */
-(NSMutableDictionary *) fetchCarDetails
{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    for(NSString *key in items)
    {
        CarConfigData *carConfig =  items [key];
        // personal car
        if ([carConfig.configType isEqualToString:@"PER_ONE"] && [carConfig.dictCarRates count] > 0)
        {
            CarDetailData * cd = [[CarDetailData alloc] init];
            [cd setCarKey:@"-1"];
            [cd setIsPreferred:@"N"];
            [cd setVehicleId:[Localizer getLocalizedText:@"Personal Car"]];
            
            dict[cd.carKey] = cd;
            
        } else if ([carConfig.aCarDetailKeys count] > 0) {
            for(NSString *key in carConfig.dictCarDetails)
            {
                CarDetailData *cd = (carConfig.dictCarDetails)[key];
                dict[cd.carKey] = cd;
            }
        }
    }
    return dict;
}












-(NSMutableDictionary *) fetchPersonalCarDetails:(NSString *)ctryCode
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		if(carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
		{
			for(NSString *key in carConfig.dictCarDetails)
			{
				CarDetailData *cd = (carConfig.dictCarDetails)[key];
				dict[cd.carKey] = cd;
			}
		}
	}
	
	return dict;
}

-(NSMutableDictionary *) fetchCompanyCarDetails:(NSString *)ctryCode
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		if(!carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
		{
			for(NSString *key in carConfig.dictCarDetails)
			{
				CarDetailData *cd = (carConfig.dictCarDetails)[key];
				dict[cd.carKey] = cd;
			}
		}
	}
	
	return dict;
}

//Get the personal carconfig
-(CarConfigData *) fetchPersonalCarConfig
{
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		return carConfig;
	}
	
	return nil;
}


-(CarDetailData *) fetchPreferredPersonalCarDetail:(NSString *)ctryCode
{
	//NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		if(carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
		{
			for(NSString *key in carConfig.dictCarDetails)
			{
				CarDetailData *cd = (carConfig.dictCarDetails)[key];
				if([cd.isPreferred isEqualToString:@"Y"])
					return cd;
			}
		}
	}
	
	return nil;
}


-(CarDetailData *) fetchCompanyCarDetail:(NSString *)carKey
{
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
		if(!carConfig.isPersonal && [carConfig.aCarDetailKeys count] > 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
		{
			for(NSString *key in carConfig.dictCarDetails)
			{
				CarDetailData *cd = (carConfig.dictCarDetails)[key];
				if([cd.carKey isEqualToString:carKey])
					return cd;
			}
		}
	}
	
	return nil;
}


-(NSString *) fetchCarReimbursementRates:(NSDate *)date isPersonal:(BOOL)isPersonal distance:(NSString *)distance carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode
{
	//float rate = 0.0;
	NSMutableDictionary *d = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableArray *aRates = [[NSMutableArray alloc] initWithObjects:nil];
	
	//if(isPersonal)
	//{
    for(NSString *key in items)
    {
        CarConfigData *carConfig =  items[key];
        
        if (!isPersonal && [ carConfig.configType isEqualToString:@"COM_FULLY"])
        {
            return [Localizer getLocalizedText: @"Fully reimbursed"];
        }
        
        if(carConfig.isPersonal==isPersonal && [carConfig.aCarDetailKeys count] == 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
        {
            CarRateData *carRateToUse = nil;
            for(NSString *carRateKey in carConfig.aCarRateKeys)
            {
                CarRateData *cRate = (carConfig.dictCarRates)[carRateKey];
                //NSLog(@"cRate.dateStart = %@", cRate.dateStart);
                if ([date compare:cRate.dateStart] == NSOrderedDescending)
                {
                    //NSOrderedDescending: //date is greater than carConfig start date
                    if(carRateToUse == nil)
                        carRateToUse = cRate;
                    else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
                        carRateToUse = cRate; //this looped car rate is closer to the date we want...
                    
                }
            }
            
            if(carRateToUse != nil)
            {
                NSString *sRate =nil;
                NSString *carFormatValue = nil;
                NSNumber *decimalRate = @([carRateToUse.rate doubleValue]);
                //MOB-9302 Display 3 decimals for USD
                if ([carConfig.crnCode isEqualToString: @"USD"])
                {
                    carFormatValue = [FormatUtils formatMoneyWithNumber:decimalRate crnCode:carConfig.crnCode decimalPlaces:3];
                    if (carFormatValue != nil && carConfig.ctryDistanceUnitCode != nil) 
                    {
                        sRate = [NSString stringWithFormat:@"%@ per %@", carFormatValue, carConfig.ctryDistanceUnitCode];
                    }
                }
                else
                {
                    carFormatValue = [FormatUtils formatMoneyWithNumber:decimalRate crnCode:carConfig.crnCode decimalPlaces:3];
                    if (carFormatValue != nil && carConfig.ctryDistanceUnitCode != nil) 
                    {
                        sRate = [NSString stringWithFormat:@"%@ per %@", carFormatValue, carConfig.ctryDistanceUnitCode];
                    }
                }
                
                if (sRate != nil)
                {
                    [aRates addObject:sRate];
                }
                break;
            }
        }
        else if(carConfig.isPersonal==isPersonal && [carConfig.aCarDetailKeys count] > 0) // && [carConfig.ctryCode isEqualToString:ctryCode])
        {
            //is variable rates
            CarDetailData *cd = (carConfig.dictCarDetails)[carKey];
            
            if(cd == nil)
            {//try to find the preferred
                for(NSString *key in carConfig.dictCarDetails)
                {
                    CarDetailData *cdFound = (carConfig.dictCarDetails)[key];
                    if([cdFound.isPreferred isEqualToString:@"Y"])
                    {
                        cd = cdFound;
                        break;
                    }
                }
            }
            
            if(carConfig.isPersonal==isPersonal && cd != nil)
            {//we have found the vehicle to get rates from...
                
                //NSMutableArray *aSorted = [cd.aCarRateTypes sortUsingSelector:@selector(crtd.lowerLimit)
                //NSMutableArray *a = cd.aCarRateTypes;
                
                //				   NSSortDescriptor *sorter = [[NSSortDescriptor alloc] initWithKey:@"rateType" ascending:YES];
                //					NSSortDescriptor *sorter2 = [[NSSortDescriptor alloc] initWithKey:@"lowerLimit" ascending:YES];
                NSSortDescriptor *sd = [[NSSortDescriptor alloc] initWithKey:@"rateType" ascending:YES];
                NSSortDescriptor *sd2 = [[NSSortDescriptor alloc] initWithKey:@"iLower" ascending:YES];
                NSArray *sortDescriptors = @[sd, sd2];
                NSArray *sortedArray = [cd.aCarRateTypes sortedArrayUsingDescriptors:sortDescriptors];
                
                //[a sortUsingDescriptors:[NSArray arrayWithObject:sorter, sorter2]];
                //[sorter release];
                
                for(int i =0; i < [sortedArray count]; i++)// CarRateTypeData *crtd in sortedArray) // cd.aCarRateTypes)
                {
                    CarRateTypeData *crtd = sortedArray[i];
                    int lower = [crtd.lowerLimit intValue];
                    int upper = [crtd.upperLimit intValue];
                    
                    if(crtd.lowerLimit == nil)
                        lower = -1;
                    
                    if(crtd.upperLimit == nil)
                        upper = -1;
                    
                    if([crtd.aCarRateKeys count] > 0) //iDistance >= lower && iDistance <= upper
                    {
                        CarRateData *carRateToUse = nil;
                        
                        for(NSString *carRateKey in crtd.dictCarRates)
                        {
                            CarRateData *cRate = (crtd.dictCarRates)[carRateKey];
                            
                            NSComparisonResult sort = [date compare:cRate.dateStart];
                            if ( sort == NSOrderedDescending || sort == NSOrderedSame)
                            {
                                //NSOrderedDescending: //date is greater than carConfig start date
                                if(carRateToUse == nil)
                                    carRateToUse = cRate;
                                else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
                                    carRateToUse = cRate; //this looped car rate is closer to the date we want...
                                
                            }
                        }
                        
                        if(carRateToUse != nil)
                        {
                            NSString *sRate = [NSString stringWithFormat:@"%d - %d %@ per %@", lower, upper, [FormatUtils formatMoney:carRateToUse.rate crnCode:carConfig.crnCode] , carConfig.ctryDistanceUnitCode];
                            if (upper == 2147483647)
                                sRate = [NSString stringWithFormat:@"%d and above %@ per %@", lower, [FormatUtils formatMoney:carRateToUse.rate crnCode:carConfig.crnCode] , carConfig.ctryDistanceUnitCode];
                            
                            if([crtd.rateType isEqualToString:@"PER_VAR_PAS"])
                                sRate = [NSString stringWithFormat:@"%@ per %@", [FormatUtils formatMoney:carRateToUse.rate crnCode:carConfig.crnCode] , [Localizer getLocalizedText: @"Passenger"]];
                            
                            d[[NSString stringWithFormat:@"%@%d", crtd.rateType, lower]] = sRate;
                            [aRates addObject:sRate];
                        }
                    }
                }
                
            }
        }
    }
	//}
	
	NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	for(int x = 0; x < [aRates count]; x++)
        //for(NSString *key in d)
	{
		if([s length] > 0)
			[s appendString:@"\n"];
		NSString *sRate = aRates[x];// [d objectForKey:key];
		[s appendString:sRate];
	}
    
	return s;
}

-(BOOL) hasAnyCompanyCarWithRates:(NSString*) crnCode
{
	BOOL isPV = NO;
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
        // Check rates availability under COM_FULLY (can enter distance, but no reimbursement) or cars availability under COM_FIXED (company variable)
		if([carConfig.crnCode isEqualToString:crnCode] &&
           (([carConfig.configType isEqualToString:@"COM_FIXED"] && [carConfig.dictCarRates count] > 0) ||
            ([carConfig.configType isEqualToString:@"COM_FULLY"] && [carConfig.dictCarDetails count] >0)))
        {
            if (([carConfig.configType isEqualToString:@"COM_FIXED"] && [carConfig.dictCarDetails count] >0))
            {
                // check rates under car details
            }
			return YES;
        }
	}
	
	return isPV;
}

-(BOOL) hasAnyPersonalsWithRates:(NSString*) crnCode
{
	BOOL isPV = NO;
	
	for(NSString *key in items)
	{
		CarConfigData *carConfig =  items[key];
        // Check rates availability under PER_ONE (fixed) or cars availability under PER_VARIABLE
		if( /* carConfig.isPersonal && */
           [carConfig.crnCode isEqualToString:crnCode] &&
           (([carConfig.configType isEqualToString:@"PER_ONE"] && [carConfig.dictCarRates count] > 0) ||
            ([carConfig.configType isEqualToString:@"PER_VARIABLE"] && [carConfig.dictCarDetails count] >0)))
        {
            if (([carConfig.configType isEqualToString:@"PER_VARIABLE"] && [carConfig.dictCarDetails count] >0))
            {
                // check rates under car details
            }
			return YES; 
        }
	}
	
	return isPV;
}
@end
