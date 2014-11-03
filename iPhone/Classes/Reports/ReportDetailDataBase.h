//
//  ReportDetailDataBase.h
//  ConcurMobile
//
//  Created by yiwen on 4/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ReportData.h"
#import "EntryData.h"

// Base MsgResponder to parse report detail data
@interface ReportDetailDataBase : MsgResponder {
	NSMutableDictionary		*rpts;
	NSMutableArray			*keys;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableString			*buildString;
	NSMutableString			*reportNameBuildString;
	BOOL					inReport, inEntry, inComment, inItemize, inFormField, inAttendee, inException;
    BOOL                    inAtnColumns; // In <ColumnDefinitions> block
    
	ReportData				*rpt;
	NSString				*roleCode;
	NSString				*rptKey;

	BOOL                    inCompanyDisbursements, inEmployeeDisbursements;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*rpts;
@property (nonatomic, strong) ReportData				*rpt;
@property (nonatomic, strong) NSString					*roleCode;
@property (nonatomic, strong) NSString					*rptKey;

@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) NSMutableString			*buildString;
@property (nonatomic, strong) NSMutableString			*reportNameBuildString;
@property BOOL inReport;
@property BOOL inEntry;
@property BOOL inComment;
@property BOOL inItemize;
@property BOOL inFormField;
@property BOOL inAttendee;
@property BOOL inException;

@property BOOL inCompanyDisbursements;
@property BOOL inEmployeeDisbursements;

+ (NSMutableDictionary*) getFormFieldXmlToPropertyMap;
+ (NSString*) getUnqualifiedName:(NSString*)qualifiedName;

-(id)init;
- (void) flushData;
-(void)fillEntry:(NSString *)string;
-(void)fillItemization:(NSString *)string;
-(void)fillAttendee:(NSString *)string entry:(EntryData*) entry;
- (NSString*) getReportElementName;

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict;
- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName;
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string;

- (void)checkIfServerHandlesAmountsForExpenseKey:(NSString *)expKey withPolicy:(NSString *)policyKey;

@end
