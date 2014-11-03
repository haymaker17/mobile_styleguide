//
//  SaveReportReceipt.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SaveReportData.h"

@interface SaveReportReceipt : SaveReportData 
{

}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
