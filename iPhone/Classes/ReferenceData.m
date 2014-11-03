//
//  ReferenceData.m
//  ConcurMobile
//
//  Created by yiwen on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReferenceData.h"


@implementation ReferenceData
@synthesize currencies, currencyDict;

ReferenceData* _refData = nil;

+(ReferenceData*)getSingleton
{
	if (_refData == nil)
	{
		_refData = [[ReferenceData alloc] init];
	}
	return _refData;
}

-(Currency*) getCurrencyFromCode:(NSString*) crnCode inLocale:(NSLocale*) locale
{
	__autoreleasing Currency* crn = [[Currency alloc] init];
	crn.crnCode = crnCode;
	crn.crnName = [locale displayNameForKey:NSLocaleCurrencyCode value:crnCode];
	return crn;
}

-(NSString *) getCurrencyCodeAtIndex:(NSUInteger) index
{
	if (currencies == nil || [currencies count] <= index)
		return nil;
	
	return ((Currency*) currencies[index]).crnCode;
}

-(Currency *) getCurrencyAtIndex:(NSUInteger) index
{
	if (currencies == nil || [currencies count] <= index)
		return nil;
	
	return (Currency*) currencies[index];
}

-(Currency *) getCurrencyByCode:(NSString*) crnCode
{
	if (currencyDict == nil)
		return nil;
	
	return (Currency *)currencyDict[crnCode];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        // Init currencies
        currencyDict = [[NSMutableDictionary alloc] init];
        NSLocale *locale = [NSLocale currentLocale];
        currencies = [[NSMutableArray alloc] initWithObjects:
				  [self getCurrencyFromCode:@"USD" inLocale:locale]
				  , [self getCurrencyFromCode:@"EUR" inLocale:locale]
				  , [self getCurrencyFromCode:@"GBP" inLocale:locale]
				  , [self getCurrencyFromCode:@"CAD" inLocale:locale]
				  , [self getCurrencyFromCode:@"MXN" inLocale:locale]
				  , [self getCurrencyFromCode:@"AUD" inLocale:locale]
				  , [self getCurrencyFromCode:@"CNY" inLocale:locale]
				  , [self getCurrencyFromCode:@"HKD" inLocale:locale]
				  , [self getCurrencyFromCode:@"INR" inLocale: locale]
				  , [self getCurrencyFromCode:@"JPY" inLocale:locale]
				  , [self getCurrencyFromCode:@"SEK" inLocale:locale]
				  , [self getCurrencyFromCode:@"SGD" inLocale:locale]
				  ,nil];
        NSMutableDictionary *dictNameKey = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        NSMutableArray *aNames = [[NSMutableArray alloc] initWithObjects:nil];
	
        NSArray *a =[NSLocale commonISOCurrencyCodes];
        for(int i = 0; i < [a count]; i++)
        {
            NSString *crnCode = a[i];
            //			if(![crnCode isEqualToString:@"USD"] & ![crnCode isEqualToString:@"EUR"] & ![crnCode isEqualToString:@"GBP"])
            {
                NSString *currencyName = [locale displayNameForKey:NSLocaleCurrencyCode value:crnCode];
                if (currencyName != nil)
                {
                    dictNameKey[currencyName] = crnCode;
                    [aNames addObject:currencyName];
                }
                else
                {
                    dictNameKey[crnCode] = crnCode;
                    [aNames addObject:crnCode];
                }
            }
        }
        [aNames sortUsingSelector:@selector(compare:)];
        for(int i = 0; i < [aNames count]; i++)
        {
            NSString *name = aNames[i];
            NSString *crnCode = dictNameKey[name];
            
            Currency* crn = [[Currency alloc] init];
            crn.crnCode = crnCode;
            crn.crnName = name;
            
            [currencies addObject:crn];
            currencyDict[crnCode] = crn;
        }
        
	}
	return self;
}


@end
