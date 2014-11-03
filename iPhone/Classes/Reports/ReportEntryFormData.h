//
//  ReportEntryFormData.h
//  ConcurMobile
//
//  Created by yiwen on 12/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportDetailDataBase.h"

@interface ReportEntryFormData : ReportDetailDataBase 
{
	NSString		*rpeKey;
	NSString		*expKey;
	NSString		*parentRpeKey;
	NSString		*includeFormDef;
}

@property(nonatomic, strong) NSString		*rpeKey;
@property(nonatomic, strong) NSString		*expKey;
@property(nonatomic, strong) NSString		*parentRpeKey;
@property(nonatomic, strong) NSString		*includeFormDef;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
