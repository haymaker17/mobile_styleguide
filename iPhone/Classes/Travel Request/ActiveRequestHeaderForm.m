//
//  ActiveRequestHeaderForm.m
//  ConcurMobile
//
//  Created by laurent mery on 18/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestHeaderForm.h"

#import "CTETravelRequest.h"
#import "CTETravelRequestComment.h"

#import "CCFormatUtilities.h"

@interface ActiveRequestHeaderForm ()

@end

@implementation ActiveRequestHeaderForm {
	
	CTETravelRequest *request;
}

#pragma mark - form & fields

//public
-(void)initFormWithDatas:(id)datas{
	
	request = (CTETravelRequest*)datas;
	[self addForm:@"requestHeader" withFormKey:request.HeaderFormKey andDatas:request];
}

//public
-(void)reloadDatas:(id)datas{
	
	request = (CTETravelRequest*)datas;
	[self updateForm:@"requestHeader" withDatas:request];
}


#pragma mark - section and fieldS

-(NSArray*)filteredFieldsInSection:(FFSection*)section{
	
	NSArray *fields = [super filteredFieldsInSection:section];
	
	NSArray *fieldsToHide = [NSArray arrayWithObjects:@"CaSection",@"EmpName",@"AgencyOfficeKey", @"AuthorizedDate", @"CaSection", @"Duration", @"EventArKey", @"HierNodeKey", @"BiHierNodeKey", @"ReqKey", @"RiskZoneId", @"SubmitDate", @"TotalApprovedAmount", @"TotalPostedAmount", @"TotalRemainingAmount",nil];
	
	for (CTEField *field in fields){
		
		//ACCESS TRANSFORM
		
		if ([@"ArPolKey" isEqualToString:field.Name]){
			
			[field setReadOnlyMax];
		}
		
		if([fieldsToHide containsObject:field.Name]){
			
			[field setAccess:@"HD"];
		}
		
	}
	
	return fields;
}

#pragma mark - Field at IndexPath

-(NSString*)valueAtIndexPath:(NSIndexPath *)indexPath{
	
	//format value / DATATYPE
	/*
	 VARCHAR(static,      edit,textarea,picklist[extended requestid])
	 INTEGER(static,      edit,picklist[MAin destination city])
	 TIMESTAMP(static,    edit,date_edit[custom field])
	 CHAR(                edit[approval status], time[time], picklist[main destination country])
	 MONEY(static,        edit)
	 BOOLEANCHAR(         checkbox,picklist)
	 LIST(                picklist,list_edit)
	 MLIST(               list_edit[connectedList])
	 */
	
	NSString *value = [super valueAtIndexPath:indexPath];
	CTEField *field = [self fieldAtIndexPath:indexPath];
	
	value = [request valueForKey:field.Name];
	
	
	//time format
	if ([@"CHAR" isEqualToString:field.DataType] && [@"time" isEqualToString:field.CtrlType]){
		
		value = [CCFormatUtilities formatedTimeHma:value withTemplate:@"hma"];
	}
	
	//date format
	if ([@"TIMESTAMP" isEqualToString:field.DataType]){
		
		value = [CCFormatUtilities formatedDate:value withTemplate:@"eeeddMMMyyyy"];
	}
	
	if ([@"Comment" isEqualToString:field.Name]) {
		
		CTETravelRequestComment *lastComment = [request getLastComment];
		if (lastComment != nil) {
			value = [lastComment getCommentTextOnly];
		}
	}
	
	return value;
}

-(void)setValue:(NSString*)value oldValue:(NSString*)oldValue atIndexPath:(NSIndexPath*)indexPath{
	
	CTEField *field = [self fieldAtIndexPath:indexPath];
	
	[request setValue:value forKey:field.Name];
	[super setValue:value oldValue:oldValue atIndexPath:indexPath];
}


@end
