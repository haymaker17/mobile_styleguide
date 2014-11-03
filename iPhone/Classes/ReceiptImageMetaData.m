//
//  ReceiptImageMetaData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptImageMetaData.h"

#define ME_KEY @"ME_KEY"
#define RPE_KEY @"RPE_KEY"
#define RPT_KEY @"RPT_KEY"
#define RS_KEY @"RS_KEY" //Receipt Store GUID

@implementation ReceiptImageMetaData
@synthesize imageName, receiptName, receiptAnnotation;
@synthesize dateModified, dateCreated;
@synthesize	key, userID;

-(ReceiptImageMetaData *) init
{
	return self;
}


-(void)setMeKey:(NSString*)meKey
{
	self.key = [NSString stringWithFormat:@"%@_%@",ME_KEY,meKey];
}

-(void)setRpeKey:(NSString*)rpeKey
{
	self.key = [NSString stringWithFormat:@"%@_%@",RPE_KEY,rpeKey];
}

-(void)setRptKey:(NSString*)rptKey
{
	self.key = [NSString stringWithFormat:@"%@_%@",RPT_KEY,rptKey];
}


-(void)setRSKey:(NSString*)rsKey
{
	self.key = [NSString stringWithFormat:@"%@_%@",RS_KEY,rsKey];
}


-(NSString*)getKeyType:(NSString*)theKey
{
	if ([theKey hasPrefix:ME_KEY])
	{
		return ME_KEY;
	}
	else if ([theKey hasPrefix:RPT_KEY])
	{
		return RPT_KEY;
	}
	else if ([theKey hasPrefix:RS_KEY])
	{
		return RS_KEY;
	}
	else {
		return nil;
	}

}




@end
