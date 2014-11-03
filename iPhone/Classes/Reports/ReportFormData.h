//
//  ReportFormData.h
//  ConcurMobile
//
//  Created by yiwen on 2/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportDetailDataBase.h"

@interface ReportFormData : ReportDetailDataBase 
{
	NSString		*polKey;
	
}
@property(nonatomic, strong) NSString		*polKey;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;


@end
