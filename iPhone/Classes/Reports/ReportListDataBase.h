//
//  ReportListDataBase.h
//  ConcurMobile
//
//  This class implements common APIs to parse a list of reports 
//  with summary entry list data
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ListDataBase.h"
#import "Msg.h"
#import "ReportData.h"
#import "EntryData.h"


@interface ReportListDataBase : ListDataBase {
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	ReportData				*rpt;
	NSMutableString			*buildString;
	NSMutableString			*reportNameBuildString;
    BOOL                    inEntry;
}

@property (nonatomic, strong) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) ReportData				*rpt;
@property (nonatomic, strong) NSMutableString			*buildString;
@property (nonatomic, strong) NSMutableString			*reportNameBuildString;
@property BOOL inEntry;

//- (void)parseXMLFileAtURL:(NSString *)URL;
- (void) respondToXMLData:(NSData *)data;
//- (void) flushData;
- (id) init;
- (NSString*) getReportElementName;
- (NSString*) getEntryElementName;

@end
