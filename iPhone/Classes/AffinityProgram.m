//
//  AffinityProgram.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AffinityProgram.h"

@implementation AffinityProgram
@synthesize accountNumber, description, programId, programName, programType, vendor, vendorAbbrev;

static NSMutableDictionary* affinityProgramXmlToPropertyMap = nil;

+ (NSMutableDictionary*) getXmlToPropertyMap
{
	return affinityProgramXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [AffinityProgram class]) 
	{
        // Perform initialization here.
		affinityProgramXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		affinityProgramXmlToPropertyMap[@"AccountNumber"] = @"AccountNumber";
		affinityProgramXmlToPropertyMap[@"Description"] = @"Description";
		affinityProgramXmlToPropertyMap[@"ProgramId"] = @"ProgramId";
		affinityProgramXmlToPropertyMap[@"ProgramName"] = @"ProgramName";
		affinityProgramXmlToPropertyMap[@"ProgramType"] = @"ProgramType";
		affinityProgramXmlToPropertyMap[@"Vendor"] = @"Vendor";
		affinityProgramXmlToPropertyMap[@"VendorAbbrev"] = @"VendorAbbrev";
    }
}


@end
